> [!warning] 2026-08-20 갱신 — iOS 한정이 해제됐다
> 아래 본문의 **"1차 범위 — iOS 뿐"** 절과 헤더 요약 마지막 문장(`1차 범위는 iOS 뿐이다`)은 더 이상 유효하지 않다.
> 지금은 **iOS·Android 모두 발송**하고 알림 종류도 3종(문의 생성 · 예약 완료 · 공동보드 댓글)이다.
> 문서 하단 **갱신 (2026-08-20)** 절을 먼저 읽을 것. 본문은 당시 판단을 남기려고 그대로 둔다.

> **ADR-021 · Infra · Accepted · 2026-08-16**
> 앱 푸시 알림(새 문의 / 예약 완료)을 어떻게 보낼지 결정. → **Expo Push 대신 FCM HTTP v1 을 Admin SDK 없이 직접 호출한다. 발송은 `AFTER_COMMIT` + `@Async` 로 비즈니스 트랜잭션에서 완전히 떼어내고, 실패는 예외 대신 결과 enum 으로 답한다. 1차 범위는 iOS 뿐이다.**

## 맥락
- 보낼 알림은 둘. **문의 생성 → 작가**, **예약 완료(RESERVED) → 고객**.
- 앱이 Expo + `react-native-webview` 껍데기라 **웹 JS 는 네이티브 푸시를 못 받는다.** 토큰 발급은 네이티브 셸이 하고 `postMessage` 로 웹에 넘긴다.
- **알림함(인앱 알림 목록)을 만들지 않기로 했다.** 사용자는 문의 목록에서 확인한다.
- 문의 생성은 이미 `InquiryCreatedEvent` 를 **동기 + 같은 트랜잭션**으로 쓰고 있다(채팅방 원자성, [[ADR-012 채팅 - 1대1 방·메시지 + 문의 자동연동 + WebSocket 수신]]). 여기에 푸시를 얹으면 안 된다.
- 심사가 iOS 쪽에서 막혀 있었다([[8.3 1차 거절]] · [[8.5 2차 거절]]).

## 결정
### FCM, 그중에서도 HTTP v1 직접 호출
- **Expo Push 기각.** 사용자 화면에 보이는 결과는 동일하지만, WatchBox(웹 전용)·데스크톱 브라우저에는 **Expo Push 를 아예 못 쓴다.** 죽은 토큰 감지도 Expo 는 ticket → receipt **2단계**라 폴링 스케줄러가 따로 필요하다.
- **Firebase Admin SDK 도 안 쓴다.** 서비스 계정 JSON → JWT assertion → access token 발급·캐싱을 `GoogleAccessTokenProvider` 로 직접 구현했다. 의존성이 줄고, 무엇보다 인증 흐름이 코드에 드러난다.
- 응답을 직접 읽어야 죽은 토큰을 가려낼 수 있어, 상태코드로 예외를 던지는 `retrieve()` 대신 **`exchange()`** 로 응답을 다룬다.

### 실패를 예외로 만들지 않는다
`PushSender.send()` 는 **예외를 던지지 않고 `PushSendResult` 로 답한다.**

| 응답 | 결과 | 처리 |
|---|---|---|
| 2xx | `SUCCESS` | — |
| `UNREGISTERED`(404) | `TOKEN_INVALID` | **즉시 토큰 삭제** (앱 삭제·재설치) |
| `INVALID_ARGUMENT`(400) | `TOKEN_INVALID` | 즉시 삭제 (형식 오류) |
| 429 / 5xx | `RETRYABLE_FAILURE` | 토큰 유지 |
| 그 외 | `FAILURE` | 토큰 유지 |

### 트랜잭션에서 완전히 떼어낸다
- **`@TransactionalEventListener(AFTER_COMMIT)` + `@Async`** — 커밋 이후여야 롤백된 문의의 알림이 안 나가고, 비동기여야 FCM 응답을 기다리는 동안 문의 API 가 느려지지 않는다.
- **리스너가 예외를 삼킨다.** 푸시 실패가 이미 커밋된 문의에 영향을 주면 안 된다.
- 푸시 전용 이벤트(`InquiryCreatedPushEvent`)를 따로 둔다 — 기존 `InquiryCreatedEvent` 는 엔티티를 담고 있어 트랜잭션 밖에서 LAZY 를 타면 터진다. **엔티티 대신 필요한 값만**(수신자 memberId, 문구용 문자열, 딥링크용 id) 담는다.

### 디바이스 토큰
- 한 회원이 기기 여러 대 → **1:N**. 토큰 문자열에 **unique**.
- 기기 하나를 두 사람이 번갈아 로그인하면 같은 토큰이 다시 올라온다. 행을 새로 만들면 **이전 소유자에게 남의 알림이 간다** → `transferTo` 로 소유자를 옮긴다.
- 로그아웃·탈퇴 시 삭제.

### 발송 로그가 유일한 추적 수단
알림함이 없어서 "알림이 안 왔어요" 를 확인할 곳이 로그뿐이다. `memberId / 종류 / 대상 건 / 결과` 를 반드시 남긴다.

### 1차 범위 — iOS 뿐
`DeviceTokenQueryService.findSendTargets()` 가 **`platform = IOS` 로 좁힌다.** 앱이 Android 에서 토큰을 요청하지 않아 실제로 저장된 Android 토큰은 없지만, **나중에 켤 때 이 필터만 걷어내면 되도록** 명시해 둔다.

## 검토한 대안
| 대안 | 기각 이유 |
|---|---|
| Expo Push | WatchBox 웹·데스크톱에 못 쓴다. 죽은 토큰 감지가 receipt 폴링 2단계라 스케줄러가 하나 더 필요 |
| APNs 직접(HTTP/2 + `.p8`) | iOS 만이면 더 단순하지만 **Android 추가 시 FCM 을 처음부터 새로 연동**해야 한다. 지금 싸고 나중에 두 배 |
| Firebase Admin SDK | 편하지만 의존성이 무겁고 인증 흐름이 가려진다. 직접 구현이 학습·통제 양쪽에서 낫다 |
| 기존 `InquiryCreatedEvent` 리스너에 푸시 추가 | 같은 트랜잭션이라 ①커넥션을 쥔 채 외부 HTTP 대기 ②롤백돼도 알림은 이미 나감 |
| 푸시 실패 시 예외 전파 | 이미 커밋된 문의가 푸시 때문에 실패로 보인다 |
| 알림함(인앱 목록) 구축 | 기획에 없다. 문의 목록에서 확인 가능하므로 도메인 하나를 통째로 아낀다 |
| Android 토큰도 저장해두고 발송만 막기 | 쓰지도 않을 개인정보를 모으는 셈이고, 토큰은 시간이 지나면 무효해져 나중에 켤 때 쓸모도 없다 |

## 결과 (트레이드오프)
- ✅ 푸시가 실패해도 문의·예약은 정상 커밋된다. 응답 속도에도 영향이 없다.
- ✅ 죽은 토큰이 발송 시점에 즉시 정리된다(별도 스케줄러 없음).
- ✅ 나중에 Android·웹 푸시를 켤 때 어댑터를 갈아끼울 필요가 없다.
- ⚠️ **Android 사용자는 아무 알림도 받지 못한다.** 작가에게 "새 문의"는 놓치면 매출로 직결되는데, Android 작가만 확인이 늦어진다 → **"알림 기능 완료"로 처리하면 안 된다.**
- ⚠️ 푸시는 **앱 설치자에게만** 간다. 웹 브라우저 사용자는 못 받는다. "앱 미설치 + Android" 는 이중 누락 구간이다.
- ⚠️ **재시도가 없다.** `RETRYABLE_FAILURE` 는 분류만 하고 실제 재시도는 안 한다 — 일시 장애 시 그 알림은 유실된다.
- ⚠️ `AndroidConfig` 페이로드는 **실전 검증이 안 된 상태**다. Android 를 켤 때 채널·priority 를 확인해야 한다.
- ⚠️ 알림 본문에 실명을 넣으면 잠금화면에 노출된다 — `"새 문의가 도착했어요 · 6/11 10:00"` 처럼 최소화한다.

## 앱 심사
- **Guideline 4.5.4** — 푸시를 앱 동작의 필수 조건으로 만들면 안 된다. 권한을 거부해도 문의 목록에서 다 확인 가능하므로 충족.
- 권한 요청은 첫 실행에 맥락 없이 띄우지 말 것. 자체 안내 후 수락한 사람에게만 OS 팝업(한 번 거부하면 다시 못 띄운다).
- 이번 두 알림은 거래 정보라 광고성 동의 대상이 아니다. 마케팅 푸시를 붙이면 별도 동의 + 야간(21~08시) 발송 제한.

## 관련
- `domain/notification/**` — 토큰 저장·조회, 메시지 조립, 발송, 리스너 2종
- `global/push/{PushSender, PushSendResult}`, `global/push/fcm/**`
- `global/config/AsyncConfig` — 푸시 전용 Executor
- `domain/inquiry/event/InquiryCreatedPushEvent`, `InquiryReservedPushEvent`

## 관련 노트
- [[푸시 알림 백엔드 설계 (FCM)]] — 착수 전 설계 노트(계획). 이 ADR 이 확정본
- [[ADR-012 채팅 - 1대1 방·메시지 + 문의 자동연동 + WebSocket 수신]]
- [[ADR-011 Apple 토큰 revoke - App Store 심사 대응(.p8 client_secret)]]

---

## 갱신 (2026-08-20) — Android 발송 개시, 알림 3종·N명 팬아웃

**본문의 "1차 범위 — iOS 뿐" 절은 더 이상 유효하지 않다.** 헤더 요약의 마지막 문장(`1차 범위는 iOS 뿐이다`)도 마찬가지다. 당시 판단을 남기려고 본문은 그대로 두고, 바뀐 것만 여기 적는다.

### Android 를 켰다 — 필터 제거 + `android` 블록 신규 작성

- `DeviceTokenQueryService.findSendTargets()` 의 `platform = IOS` 필터를 걷어냈다. **한 회원이 iOS·Android 기기를 함께 쓸 수 있으므로** 플랫폼을 가리지 않는다.
- `FcmMessageMapper` 에 `android` 블록을 새로 썼다. 본문 트레이드오프에는 "`AndroidConfig` 페이로드는 실전 검증이 안 된 상태"라고 적어뒀지만, **확인해보니 그 코드는 존재한 적이 없었다** — 검증이 아니라 신규 작성이었다.

| 필드 | 왜 필요한가 |
|---|---|
| `channel_id` | Android 8+ 는 채널이 없으면 **알림이 아예 표시되지 않는다.** 앱이 notifee 로 만드는 채널(`nativePush.ts` 의 `ANDROID_CHANNEL_ID`)과 어긋나면 **에러 없이 누락되고 발송 로그는 SUCCESS 로 남는다** — 상수로 고정하고 테스트로 묶었다 |
| `priority: high` | 기본값은 지연 전송될 수 있다 |
| `default_sound` | 없으면 무음으로 도착한다 |

- **`apns` 와 `android` 블록을 한 요청에 함께 싣는다.** FCM 이 대상 토큰의 플랫폼에 맞는 블록만 골라 쓰므로 **요청 하나로 양쪽을 커버**한다. 서버는 플랫폼을 분기하지 않는다.
- 그래서 **`DevicePlatform` 값은 발송 경로에 쓰이지 않는다.** 통계·디버깅용으로만 남는다.

### 세 번째 알림 — 공동보드 댓글, 그리고 첫 N명 팬아웃

본문의 두 알림(문의 생성 → 작가, 예약 완료 → 고객)은 **수신자가 한 명**이었다. 공동보드 댓글은 다르다.

- **작성자를 뺀 참여자 전원**에게 보낸다. 수신자 수가 보드마다 다르고 초대로 계속 늘어난다([[ADR-016 공동보드 - 보드당 작품 1회 담기와 보드 단위 댓글]]).
- **수신자 목록을 이벤트에 담지 않는다.** 댓글 등록 트랜잭션에 조회를 얹지 않으려고, 리스너가 자기 트랜잭션(`REQUIRES_NEW`)에서 조회한다. 이벤트에는 **누구를 빼야 하는지**(`authorCustomerId`)만 싣는다. 문구용 `authorName` 은 이미 로딩된 값이라 그대로 실어 재조회를 없앤다.
- **한 명에게 실패해도 나머지는 계속 보낸다.** 기기 하나가 망가졌다고 보드 전체가 알림을 못 받으면 안 된다.
- 수신자가 없으면(혼자 쓰는 보드) `info` 로 남기고 끝낸다 — 정상 상황이라 경고로 올리지 않는다.

### 해소된 것

- ✅ **"Android 사용자는 아무 알림도 받지 못한다"** — 해소. 본문에 "알림 기능 완료로 처리하면 안 된다"고 적어둔 조건이 충족됐다.
- ✅ **"`AndroidConfig` 페이로드는 실전 검증이 안 된 상태"** — 해소(신규 작성 + 테스트).
- ✅ 본문의 "앱 미설치 + Android 이중 누락 구간" 에서 **Android 축이 빠졌다.** 남은 건 앱 미설치 축뿐이다.

### 새로 생긴 트레이드오프

- ⚠️ **N명 팬아웃이 푸시 스레드풀을 오래 점유한다.** 참여자 수만큼 FCM 왕복이 순차로 돈다. 풀은 core 2 / max 4 이고 큐가 차면 `DiscardPolicy` 로 **버린다**(`AsyncConfig`) — 큰 보드에서 댓글이 몰리면 뒤쪽 알림이 조용히 사라질 수 있다. 발송을 배치(FCM multicast)로 바꾸거나 큐 정책을 재검토할 지점.
- ⚠️ **채널 ID 가 서버와 앱, 두 레포에 나뉘어 있다.** 한쪽만 바꾸면 Android 알림이 조용히 멈추는데 **로그는 SUCCESS 로 남아** 알아채기 어렵다. 양쪽 상수에 서로를 가리키는 주석을 달아뒀지만 **컴파일러도 CI 도 잡아주지 않는다.**
- ⚠️ **재시도가 없다는 점은 그대로인데 유실 가능 구간이 넓어졌다.** 수신자가 늘수록 `RETRYABLE_FAILURE` 로 떨어지는 건수도 같이 는다.

### 그대로 남은 것

- ⚠️ 웹 브라우저 사용자는 여전히 못 받는다. 푸시는 앱 설치자에게만 간다.
- ⚠️ `RETRYABLE_FAILURE` 는 여전히 분류만 하고 실제 재시도는 하지 않는다.
- ⚠️ 알림함이 없어 발송 로그가 유일한 추적 수단이라는 점도 그대로다.

## 관련 (갱신분)

- `global/push/fcm/FcmMessageMapper#{apnsConfig, androidConfig}`, `NOTIFICATION_CHANNEL_ID`
- `domain/notification/service/DeviceTokenQueryService#findSendTargets` — 플랫폼 필터 제거
- `domain/notification/entity/DevicePlatform` — 발송 분기에 쓰이지 않음을 명시
- `domain/board/event/SharedCommentCreatedPushEvent`, `domain/notification/event/SharedCommentPushListener`
- `test/domain/notification/message/PushMessageFactoryCommentTest`
- 앱 — `apps/mobile/nativePush.ts` 의 `ANDROID_CHANNEL_ID` (서버 상수와 반드시 일치)

## 관련 노트 (갱신분)

- [[ADR-016 공동보드 - 보드당 작품 1회 담기와 보드 단위 댓글]]
- [[ADR-022 읽음 상태 - 채팅은 카운터, 공동보드 댓글은 시각 커서]]
