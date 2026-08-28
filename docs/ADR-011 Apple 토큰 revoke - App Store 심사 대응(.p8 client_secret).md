> **ADR-011 · Auth · Accepted · 2026-07-24**
> Sign in with Apple 계정의 탈퇴 시 토큰 revoke(App Store 심사 필수)를 어떻게 구현할지 결정. → **로그인 때 authorization code 를 refresh token 으로 교환·저장해두고, 탈퇴 때 .p8 기반 ES256 client_secret 으로 `/auth/revoke` 호출. 구글은 revoke 안 함.**

## 맥락
- Apple 은 2022.6~ **인앱 계정 삭제 시 Apple 토큰 revoke 를 심사 필수**(Guideline 5.1.1(v)). 안 하면 리젝.
- 기존 애플 로그인은 identityToken 을 JWKS 로 검증만 함([[ADR-004 소셜 로그인 - 구글·애플 네이티브·웹]]) → **.p8/client_secret 없음, refresh token 도 없음** → revoke 불가 상태였음.
- revoke 하려면 ① client_secret(ES256, .p8 서명) ② 폐기할 refresh token 이 필요. refresh token 은 authorization code 교환으로만 얻음 → **로그인 플로우 변경 불가피**.
- 탈퇴([[ADR-010 회원 탈퇴·역할 해지 - soft delete·익명화·문의 자동취소]])와 **동시 구현**(별도·후순위 아님) — 탈퇴만 있고 revoke 없으면 심사 통과 못 함.

## 결정
- **client_secret 생성** — team-id/key-id/private-key(.p8 PEM)로 ES256 서명 JWT 생성(Nimbus). aud=`appleid.apple.com`, exp=+5분, sub=clientId. `AppleClientSecretGenerator`.
- **Apple 토큰 엔드포인트 클라이언트** — `AppleTokenService`(RestClient): `exchangeAuthorizationCode`(code→refresh token), `revoke`(`/auth/revoke`).
- **로그인 시 refresh token 확보·저장**
    - 네이티브: `NativeLoginRequest.authorizationCode` 추가 → `apple.native.client-id` 로 교환.
    - 웹: form_post 콜백에서 `code` 캡처 → `apple.web.client-id`/redirect-uri 로 교환.
    - `OAuthAccount.oauthRefreshToken`·`oauthRefreshClientId` 컬럼에 저장. **교환 실패해도 로그인은 계속**(탈퇴 시 revoke 만 스킵).
- **탈퇴 시 revoke** — `OAuthAccountService.revokeAppleTokenIfPresent`: APPLE + refresh token 있을 때만 호출. `MemberFacade.withdraw` 첫 단계, **외부호출이라 실패해도 탈퇴 진행**(로그만).
- **client_id 는 저장한 값을 사용** — 네이티브/웹이 서로 다른 client_id(Bundle ID vs Services ID)라, 교환에 쓴 client_id 를 함께 저장(`oauthRefreshClientId`)해 revoke 때 그대로 사용.
- **Google 은 revoke 안 함**(확정) — OAuthAccount 삭제 + 세션 삭제로 충분, 심사 무관.

## 검토한 대안
| 대안 | 기각 이유 |
|---|---|
| revoke 를 후속 태스크로 미룸 | App Store 리젝 사유. 탈퇴와 한 몸이라 함께 가야 함 |
| Apple refresh token 을 로그인 유지에도 활용 | 우리 인증은 자체 JWT. Apple refresh token 은 **오직 revoke 용도**로만 저장 |
| .p8 대신 일반 OAuth client secret | Sign in with Apple 은 .p8 ES256 client_secret 강제. 일반 secret 개념 없음 |
| revoke 실패 시 탈퇴 롤백 | 외부 장애로 탈퇴가 막히면 안 됨(삭제권). 실패는 로그 + 수동 대응, 탈퇴는 진행 |
| client_id 를 설정에서 단일 고정 | 네이티브/웹 client_id 다름 → 교환에 쓴 값을 저장해야 revoke 매칭 |

## 결과 (트레이드오프)
- ✅ 심사 3요건 충족: 인앱 탈퇴(`DELETE /api/members`) + PII 익명화 + Apple revoke.
- ✅ refresh token 을 저장만 하고 revoke 전용 → 자체 JWT 인증과 독립.
- ✅ 교환/폐기 실패가 로그인·탈퇴를 막지 않음(best-effort).
- ⚠️ **.p8 개인키·team/key-id 를 env 로 관리** 필요: `APPLE_TEAM_ID`/`APPLE_KEY_ID`/`APPLE_PRIVATE_KEY`/`APPLE_NATIVE_CLIENT_ID`. 미설정 시 client_secret 생성 실패.
- ⚠️ **프론트 의존** — 앱이 로그인 때 `authorizationCode` 를 보내야 refresh token 이 저장됨. 안 보내면 그 계정은 탈퇴 시 revoke 스킵(심사 리스크).
- ⚠️ authorization code 는 일회성·단수명 → 로그인 시점에만 교환 가능(놓치면 재로그인 전까지 refresh token 없음).

## 관련
- `domain/auth/service/custom/{AppleClientSecretGenerator, AppleTokenService}`
- `domain/auth/entity/OAuthAccount`(oauthRefreshToken/oauthRefreshClientId), `service/OAuthAccountService`(updateRefreshToken/revokeAppleTokenIfPresent)
- `domain/auth/facade/AuthFacade`(네이티브 교환), `global/auth/oauth/custom/AppleWebLoginService`(웹 교환), `controller/SocialLoginController`(code 캡처)
- `domain/auth/dto/NativeLoginRequest.authorizationCode`, `application.yml`(apple.*), `ErrorCode.INTERNAL_SERVER_ERROR`

## 관련 노트
- [[ADR-010 회원 탈퇴·역할 해지 - soft delete·익명화·문의 자동취소]]
- [[ADR-004 소셜 로그인 - 구글·애플 네이티브·웹]]
- [[회원 탈퇴 설계 (백엔드)]]
