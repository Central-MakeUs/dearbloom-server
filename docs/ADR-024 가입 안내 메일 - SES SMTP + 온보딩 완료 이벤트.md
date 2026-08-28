> **ADR-024 · Infra · Accepted · 2026-08-19**
> 가입 안내 메일을 어떻게 보낼지 결정. → **SES 를 SDK 가 아니라 SMTP 로 붙여 발송처를 설정으로만 갈아끼운다. 트리거는 소셜 로그인이 아니라 온보딩 완료 시점의 이벤트이고, `AFTER_COMMIT` + 전용 스레드풀로 비동기 발송한다. Apple 이 이메일을 주지 않아 만들어 넣은 주소는 발송 전에 거른다.**

## 맥락
- 온보딩을 마친 사용자에게 안내 메일이 필요하다.
- 형태는 이미 푸시에서 한 번 잡아뒀다 — 비즈니스 트랜잭션과 분리해 커밋 후 비동기로 보낸다([[ADR-021 푸시 알림 - FCM HTTP v1 직접 호출, 1차 iOS 한정]]).
- 그런데 메일에는 푸시에 없는 제약이 둘 있다.
    - **주소가 실재하지 않을 수 있다.** Apple 은 사용자가 동의했을 때만 `email` 클레임을 준다. 클레임이 없으면 `AppleNativeAuthService` 가 `OAuthAccount.email` 이 NOT NULL 이라 **자리를 채우려고 주소를 만들어 넣는다**([[ADR-004 소셜 로그인 - 구글·애플 네이티브·웹]]). 그 주소로 보내면 전량 반송되고, 반송률이 오르면 발송 서비스가 계정을 정지시킨다.
    - **실패를 되돌릴 수 없다.** 푸시는 놓쳐도 되지만 메일은 한 번 나가면 취소가 안 된다.

## 결정
### 발송 — SES SMTP
- **AWS SDK 가 아니라 `JavaMailSender`.** 발송처는 `spring.mail.*` 설정으로만 갈리고 `MailSendService` 코드는 바뀌지 않는다. SES 리전을 옮기거나 Gmail 로 임시 전환하는 게 설정 변경이다.
- **타임아웃 3종(connection/read/write) 5초.** JavaMail 기본값이 무한이라 SMTP 가 물리면 메일 스레드가 영영 잡힌다.
- `app.mail.enabled` 기본값 false — 로컬·테스트에서 실제 주소로 메일이 나가는 사고를 막는다.
- 제목에 환경명을 붙인다(운영만 생략). 개발 서버 메일과 운영 메일이 수신함에서 섞이지 않게.

### 트리거 — 소셜 로그인이 아니라 온보딩 완료
```
MemberSignedUpEvent(memberId, role, profileName)
```
- **로그인 직후(`createMember`)가 아니다.** 그 시점엔 역할이 없어 고객/작가별 내용을 고를 수 없고, **온보딩 도중 이탈한 사람에게도 메일이 나간다.**
- **엔티티가 아니라 값만 싣는다.** 수신이 비동기라 detached 엔티티의 LAZY 필드를 건드리면 터진다.
- **메일 주소는 수신 측이 자기 트랜잭션에서 조회한다.** 온보딩 응답을 그만큼 늦출 이유가 없다.

### 수신 — `AFTER_COMMIT` + `@Async` + `REQUIRES_NEW`
세 어노테이션이 각각 다른 문제를 막는다.
- `AFTER_COMMIT` — 커밋 전에 보내면 **롤백된 가입의 메일**이 나간다.
- `@Async(MAIL_EXECUTOR)` — SMTP 왕복이 수백 ms~수 초다. 동기면 온보딩 응답이 그만큼 늦어진다.
- `REQUIRES_NEW` — AFTER_COMMIT 은 원 트랜잭션이 이미 커밋된 뒤라 거기 참여할 수 없고, **Spring 이 그 조합을 기동 시점에 막는다.** 주소 조회용으로 새 트랜잭션을 연다.
- 예외를 삼킨다. 이미 커밋된 가입에 메일 실패가 영향을 주면 안 된다.

### 스레드풀 — 푸시와 나누되 거부 정책은 반대로
| | 푸시 | 메일 |
|---|---|---|
| 풀 | `pushTaskExecutor` (2~4) | `mailTaskExecutor` (1~2) |
| 큐가 찼을 때 | `DiscardPolicy` — 버린다 | `CallerRunsPolicy` — 호출 스레드가 마저 보낸다 |

풀을 나누는 이유는 **격리**다(SMTP 가 느려질 때 푸시까지 막히면 안 된다). 거부 정책이 갈리는 이유는 **알림의 성격**이다 — 푸시 하나를 놓치는 건 감수할 수 있지만, **가입 안내 메일은 버리면 사용자가 못 받은 사실조차 알 수 없다.**

### 보낼 수 없는 주소 거르기 — `ApplePrivateRelayEmail`
- **도메인으로 거르면 안 된다.** "이메일 가리기" 를 고른 사용자에게 Apple 이 주는 **진짜 중계 주소도 같은 도메인**(`@privaterelay.appleid.com`)을 쓴다. 도메인으로 거르면 정작 보낼 수 있는 사용자가 통째로 막힌다.
- 대신 **로컬파트가 `sub`(= `OAuthAccount.oauthId`)와 같은지**로 가른다. Apple 이 발급하는 진짜 중계 주소의 로컬파트는 sub 와 무관한 난수라 절대 일치하지 않는다.
- **만드는 쪽과 거르는 쪽을 한 클래스에 둔다.** 떨어져 있으면 한쪽 형식만 바뀌었을 때 조용히 전부 발송된다.

## 검토한 대안
| 대안 | 기각 이유 |
|---|---|
| SES SDK(v2) 직접 호출 | 발송처를 바꾸려면 코드를 고쳐야 한다. SMTP 는 설정만으로 다른 리전·다른 공급자로 간다 |
| 소셜 로그인 직후 발송 | 역할이 없어 내용을 고를 수 없고, **온보딩 이탈자에게도 나간다** |
| 이벤트에 `Member` 엔티티를 싣기 | 비동기 수신에서 detached LAZY 를 건드려 터진다 |
| 동기 발송 | 온보딩 응답에 SMTP 왕복이 그대로 얹힌다 |
| `@EventListener` / `BEFORE_COMMIT` | 롤백된 가입의 메일이 나간다. 메일은 취소가 안 된다 |
| 푸시와 스레드풀 공유 | SMTP 지연이 푸시 발송까지 막는다 |
| 메일도 큐가 차면 버리기 | 못 받은 사실을 사용자도 서버도 모른다 |
| 도메인으로 Apple 주소 거르기 | 배달되는 진짜 중계 주소까지 막힌다 |
| `OAuthAccount.email` 을 nullable 로 바꿔 placeholder 자체를 없애기 | 스키마·기존 데이터·프로필 응답이 함께 바뀐다. 별도 결정으로 미룸 |
| 메일 발송을 MQ 로 | 지금은 가입 한 건당 한 통이다. 브로커를 하나 더 얹을 양이 아니다 |

## 결과 (트레이드오프)
- ✅ 발송처 교체가 **설정 변경**으로 끝난다.
- ✅ 롤백된 가입의 메일이 나가지 않는다.
- ✅ 온보딩 응답 시간에 SMTP 왕복이 들어가지 않는다.
- ✅ Apple 미제공 주소로 인한 반송(→ 계정 정지)을 막는다.
- ⚠️ **발송 실패가 로그로만 남는다.** 재시도도, 실패 목록도, 관리자 알림도 없다 — 사용자는 못 받은 걸 알 수 없다. 지금 규모에선 감수하지만 **가입 전환을 메일에 의존하게 되면 재시도가 먼저 필요하다.**
- ⚠️ **큐가 차면 `CallerRunsPolicy` 가 온보딩 응답을 붙잡는다.** 호출 스레드는 커밋을 끝낸 요청 스레드다 — 버리지 않는 대가로 느려진다. 큐 200이 찰 정도면 이미 비정상이라는 전제.
- ⚠️ `MAIL_ENABLED` 기본값이 false라 **운영에서 켜는 걸 잊으면 조용히 안 나간다.** 로그에도 "비활성" 한 줄뿐이다.
- ⚠️ placeholder 판정이 **Apple 의 sub 형식에 의존**한다. Apple 이 중계 주소 규칙을 바꾸면 판정이 흔들린다.
- ⚠️ 메일 본문이 자바 문자열의 HTML 이다. 디자인이 바뀔 때마다 배포가 필요하다.

## 관련
- `domain/notification/service/MailSendService` — SMTP 발송, 환경별 제목 접두어
- `domain/notification/event/SignUpMailListener#onMemberSignedUp`
- `domain/member/event/MemberSignedUpEvent`, `domain/member/facade/MemberFacade#{createCustomer, createArtist}`
- `domain/auth/util/ApplePrivateRelayEmail#{placeholderFor, isPlaceholder}`
- `global/config/AsyncConfig#mailTaskExecutor`, `global/properties/MailProperties`
- `domain/notification/message/SignUpMailFactory`

## 관련 노트
- [[ADR-021 푸시 알림 - FCM HTTP v1 직접 호출, 1차 iOS 한정]]
- [[ADR-004 소셜 로그인 - 구글·애플 네이티브·웹]]
- [[ADR-011 Apple 토큰 revoke - App Store 심사 대응(.p8 client_secret)]]
