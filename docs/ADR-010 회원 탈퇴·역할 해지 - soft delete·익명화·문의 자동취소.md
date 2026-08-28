> **ADR-010 · Domain · Accepted · 2026-07-24**
> 소셜 계정의 '계정 종료'를 어떻게 둘지 결정. → **회원 탈퇴 = Member soft delete + OAuthAccount hard delete + 프로필 익명화, 역할 해지 = 프로필 1개만 익명화·플래그 해제(마지막 역할이면 탈퇴로 수렴), 나가는 흐름 모두 진행 중 문의를 자동 취소하고 사유를 기록.**

## 맥락
- 로그인 수단 `OAuthAccount`=멤버당 1개, 그 아래 프로필 최대 2개(Customer·Artist). [[ADR-003 Party-Role 다중역할 모델링 + 역할 전환]]
- 소셜을 반만 끊을 수 없어 **회원 탈퇴는 계정 전체**(두 프로필 + 소셜연결). 그런데 dual-role 구조라 "작가만 접고 고객으론 남기"가 실제 필요 → **역할 단위 해지가 별도로 필요**(YAGNI 아님).
- Inquiry/Review 가 Customer/Artist 를 FK 참조 → 프로필 물리삭제는 cascade 지옥. 문의는 스냅샷 기반이라([[ADR-009 스마트 문의·예약 - 상태 단일화와 슬롯 잠금]]) 표시값은 이미 보존됨.
- 결제 없음·예약 완료가 종착([[결제 없음·예약 완료가 마지막]]) → 진행 중 커밋(RESERVED)을 남기고 떠나면 상대방·슬롯이 유령 상태로 남음.

## 결정
- **회원 탈퇴(soft delete)** — `Member.withdrawnAt` 세팅 + 멤버 PII(email·name) 제거, `isEnabled()=!isWithdrawn()`.
    - `OAuthAccount` **hard delete** → 재로그인 시 신규 멤버(깨끗한 재가입).
    - 보유 프로필 **익명화**(Customer.name="탈퇴한 사용자", Artist.nickname="탈퇴한 작가", intro/image/etcInfo/travelFee 비움) — 행은 유지(FK 보존).
    - Redis refresh 세션 삭제(전 기기 즉시 로그아웃).
    - **로그인 게이트** — soft delete라 토큰만으론 못 막음 → `TokenAuthenticationFilter` 에서 withdrawn 거부(`WITHDRAWN_MEMBER` 401).
    - 엔드포인트 `DELETE /api/members`.
- **역할 해지** — 프로필 1개만 익명화 + `has_customer`/`has_artist` 플래그 해제 + recentRole 이 해지 역할이면 남은 역할로 보정 + **남은 역할로 accessToken 재발급**(`RoleRevokeResponse{withdrawn:false, accessToken, activeRole}`).
    - `@AuthenticationPrincipal Member` 로 받음 → **반대 모드로 로그인 중에도 호출 가능**(해지에 그 모드 강제 안 함).
    - 엔드포인트 `DELETE /api/customers/me`, `DELETE /api/artists/me`.
- **마지막 역할 해지 = 회원 탈퇴로 수렴**(A안) — 남은 역할이 없으면 `withdraw()` 호출, `RoleRevokeResponse{withdrawn:true}`. → 역할 해지가 탈퇴를 재사용(탈퇴가 선행 로직).
- **진행 중 문의 자동 취소**(탈퇴·해지 공용) — 나가는 당사자에게 걸린 문의를 `IN_PROGRESS→문의취소`, `RESERVED→예약취소`(슬롯 자동 해제). 탈퇴는 고객·작가 양쪽, 해지는 해당 역할만. `InquiryHistory` 에 **나가는 당사자 role + 시스템 사유**("회원 탈퇴로 자동 취소" 등) append, 응답에도 노출. 익명화 **전에** 실행, 같은 트랜잭션.
- **재온보딩 reactivate** — 해지는 행을 지우지 않고 익명화하므로, 같은 역할 재온보딩 시 익명화된 행을 되살림(같은 사람 복귀 = 문의·작품 이력 유지). `create` 가 행 존재 + 보유 플래그로 신규/재활성/중복(409) 구분, 온보딩에서 `create` 를 `markAs*` 앞으로 이동.

## 검토한 대안
| 대안 | 기각 이유 |
|---|---|
| 프로필 hard delete | Inquiry.customer_id/artist_id NOT NULL FK → 삭제 불가. 익명화로 PII만 제거 |
| 역할 해지 없이 탈퇴만 | "작가만 접기" 불가 → 계정 통째로 날리는 수밖에. dual-role 구조상 반쪽 |
| 마지막 역할 해지 차단("탈퇴 쓰세요") | 사용자 흐름 끊김. 어차피 결과가 같으니 탈퇴로 수렴이 매끄러움 |
| 진행 중 예약 있으면 탈퇴 차단 | 계정삭제를 막는 dark pattern(삭제권·Apple 심사 위험). 결제 없어 취소 비용 0 → 자동취소가 정답 |
| 자동취소를 사유 없이 상태만 변경 | 상대방이 "왜 취소됨?" 모름 → reason 으로 audit |

## 결과 (트레이드오프)
- ✅ 탈퇴/해지가 익명화 + 플래그 + 세션 + 문의취소를 한 트랜잭션으로 → 부분 실패 없음.
- ✅ 스냅샷 + 익명화로 상대방 화면 안 깨지고 FK 무결성 유지.
- ✅ 역할 해지가 탈퇴 로직 재사용(마지막 역할 수렴) → 중복 없음.
- ✅ 자동취소 사유 기록으로 상대방이 취소 경위 확인.
- ⚠️ soft delete 톰스톤 누적 — 복구는 안 함(재가입=새 계정). 정리 배치는 후속.
- ⚠️ 해지 후 재발급 전까지 남은 옛 accessToken 은 만료까지 유효(stateless) — 역할 전환과 동일한 staleness 수용([[ADR-007 소셜 로그인 role 선택 + 토큰 activeRole 확정 규칙]]).
- ⚠️ 알림 시스템 없음 → 자동취소된 상대방은 다음 조회 때 인지. 알림은 후속 태스크.

## 관련
- `domain/member/{entity/Member, service/MemberCommandService, facade/MemberFacade, controller/MemberController, dto/RoleRevokeResponse}`
- `domain/customer/{entity/Customer, service/CustomerCommandService, controller/CustomerController}` + `domain/artist/...` 대응
- `domain/inquiry/service/InquiryWithdrawalService`, `InquiryHistory.reason`, `InquiryRepository.findBy{Customer,Artist}IdAndStatusIn`
- `global/auth/jwt/TokenAuthenticationFilter`(withdrawn 게이트), `ErrorCode.WITHDRAWN_MEMBER`

## 관련 노트
- [[ADR-011 Apple 토큰 revoke - App Store 심사 대응(.p8 client_secret)]]
- [[ADR-003 Party-Role 다중역할 모델링 + 역할 전환]]
- [[ADR-009 스마트 문의·예약 - 상태 단일화와 슬롯 잠금]]
- [[ADR-007 소셜 로그인 role 선택 + 토큰 activeRole 확정 규칙]]
- [[회원 탈퇴 설계 (백엔드)]]
- [[결제 없음·예약 완료가 마지막]]

---

## 갱신 (2026-08-09) — 삭제 범위 확대, "soft delete" 프레이밍 수정

최초 결정은 **익명화 중심**이었다. `Member` soft delete + `OAuthAccount` hard delete + 프로필 익명화가 전부였고, 그 외 데이터는 손대지 않았다. 이후 점검에서 **부속 데이터와 S3 객체가 전부 남아 있다**는 것이 드러나 삭제 범위를 넓혔다.

제목의 `soft delete` 는 **`Member` 행 하나에만 해당**한다. 지금은 행 유지(익명화) / 행 삭제 / S3 객체 삭제가 섞인 **혼합 방식**이다.

### 무엇이 문제였나
- **S3 객체가 전혀 지워지지 않았다.** `Artist.anonymize()` 가 `imageUrl = null` 로 컬럼만 비웠을 뿐, CDN URL 을 아는 사람은 얼굴 사진에 영구히 접근할 수 있었다. App Store 심사에서 정면으로 지적되는 지점.
- **탈퇴한 작가의 작품이 목록에 계속 노출되고 문의까지 들어왔다.**
- 저장 작품·일정 규칙·신고가 그대로 남았다.
- `Customer.anonymize()` 가 `region` 을 비우지 않았다(`Artist` 는 처리하는데 `Customer` 만 누락).

### 추가된 결정
- **행 삭제 확대** — 신고(`Report`) / 저장 작품(`SavedArtwork`) / 작가 일정 규칙(`ArtistScheduleRule`) / 작품 전체(`Artwork`+`ArtworkPackage`+`PortfolioFile`) / 공동보드.
- **S3 객체 삭제** — 작품 사진 / 작가 대표 이미지 / 탈퇴자가 올린 채팅 사진. **행을 지우기 전에 URL 을 먼저 수집**해야 한다.
- **S3 실패는 로그만 남기고 진행**(`FileCleaner`). 외부 장애로 탈퇴가 막히면 안 된다 — Apple 토큰 revoke 와 같은 판단.
- **채팅 사진은 탈퇴자가 올린 것만**(`senderRole` 기준). 대화 행 자체는 상대방 이력이라 유지.
- **공동보드** — 혼자면 보드 삭제, 남는 멤버가 있으면 **가장 먼저 입장한 사람에게 방장 위임** 후 본인 흔적만 삭제. 방장은 나갈 수 없게 막혀 있어([[ADR-016 공동보드 - 참여자별 공유작품 행과 보드 단위 댓글]]) 위임하지 않으면 아무도 지우지 못하는 보드가 영구히 남는다.
- **`Customer.anonymize()` 에 `region` 추가.** `profileColor` 는 서버가 배정한 기본 아바타라 PII 가 아니고, 비우면 상대방 화면이 깨지므로 유지.

### 추가된 트레이드오프
- ⚠️ **작품 삭제가 취소 불가**다. 재가입해도 작품·사진은 돌아오지 않는다.
- ⚠️ 채팅 이미지 메시지는 **행은 남고 객체는 없어** 상대방 화면에서 깨진다. "삭제된 이미지" 플레이스홀더가 필요하다.
- ⚠️ **S3 삭제가 트랜잭션 커밋 전에 일어난다.** 뒤에서 롤백되면 DB 는 되돌아가는데 객체는 이미 없다. 뒤에 남은 작업이 익명화·세션 삭제뿐이라 실질 위험은 낮지만, 엄밀히 하려면 `AFTER_COMMIT` 으로 빼야 한다.
- ⚠️ 작품 수만큼 루프를 돈다. 규모가 커지면 벌크 삭제로 전환 필요.

### 아직 안 한 것
- 역할 해지 API 는 여전히 주석 처리 상태(`522cb0f`). 위 확대는 **회원 탈퇴 경로에만** 적용됐다 — 해지를 되살릴 때 같은 정리를 붙여야 한다.

데이터별 상세 규정은 [[회원 탈퇴 데이터 처리 규정]] 참고. 코드(`MemberFacade.withdraw`)에는 그 문서를 가리키는 한 줄만 남겼다.
