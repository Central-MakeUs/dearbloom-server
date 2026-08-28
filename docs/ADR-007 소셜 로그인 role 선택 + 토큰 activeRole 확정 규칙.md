> **ADR-007 · Auth · Accepted · 2026-07-22**
> 소셜 로그인·토큰 발급 전 경로에 "고객/작가 role 선택"을 도입하고, 토큰 `activeRole` 을 무엇으로 확정할지 규칙을 정함. → **"선택 의도로서의 role"과 "토큰 activeRole"을 분리. 요청이 지정한 role(override)만으로 activeRole 을 정하고, recentRole 은 토큰 결정에서 배제.**

## 맥락
- [[ADR-004 소셜 로그인 - 구글·애플 네이티브·웹]] 로 로그인 흐름은 잡혔으나, 로그인 화면에서 **고객/작가를 무조건 하나 선택**하는 요구가 추가됨.
- 위험: 신규(프로필 없음) 유저에 `activeRole=CUSTOMER` 를 박으면 이후 `@CurrentCustomer` 리졸버가 `activeProfileId=null` 로 깨진다([[ADR-006 역할별 인증 파라미터 리졸버 3종 설계]]). → "선택 의도"와 "실제 권한(activeRole)"을 반드시 분리해야 함.
- 기존 `makeToken` 은 `override → recentRole → 보유 role 첫 번째` 3단 폴백이었는데, **프론트가 매 요청 role 을 보내는 방향**으로 가면 recentRole 가지는 정상 흐름에서 쓸 일이 없어짐.
- 사전 설계: [[소셜 로그인 요청에 고객·작가 Role 선택 포함]]. 이 ADR 은 그 구현 결과를 확정 기록.

## 결정
1. **role 은 두 의미로 분리.** "선택한 role"(온보딩 라우팅 힌트) ≠ 토큰 `activeRole`(실제 권한).
2. **activeRole 확정 규칙** — `AuthService.resolveActiveRoleForLogin(member, selectedRole)`:
   - 선택 role 의 **프로필이 있으면** override = 그 role.
   - **없으면(온보딩 전)** override = `null` → 토큰 activeRole 미강제(불일치 방지).
3. **온보딩 정보 응답**
   - 네이티브 `POST /api/auth/login`: 바디에 `role` 추가, 응답 바디 `SocialLoginResponse { selectedRole, hasCustomer, hasArtist, needsOnboarding }`.
   - 웹 `GET /oauth2/{provider}/authorize?role=`: 콜백 후 프론트 콜백 URL 에 `?role=&needsOnboarding=` 쿼리로 반환.
4. **웹 role 릴레이 = `signup_role` 쿠키** (SameSite=None; Secure, 5분). OAuth 왕복에서 role 을 콜백까지 나름. (사전 설계의 `state` 릴레이 대신 채택 — 아래 대안 참고)
5. **리프레시도 role 을 받는다.** `POST /api/members/refresh { refreshToken, role }` → `switchActiveRole` 로 프로필 검증(미보유 시 403 `ROLE_NOT_AVAILABLE`) 후 그 role 로 accessToken 발급. 리프레시는 로그인 화면을 안 거치므로 role 을 명시받아야 함.
6. **`recentRole` 은 토큰 activeRole 결정에서 제거.** 로그인·전환·온보딩·리프레시 **모든 발급 경로가 override 를 명시**하므로 `makeToken` 은 `override → (엣지) 보유 role 첫 번째 → null` 로 단순화. `recentRole` 컬럼·갱신은 **"최근 접속 role 확인용(audit)"** 으로만 유지, 읽지 않음.
7. **role 타입은 기존 `MemberRole` 재사용, `@NotNull` 필수.**

## 검토한 대안
| 대안 | 기각 이유 |
|---|---|
| 신규 유저에도 선택 role 을 activeRole 로 박기 | 프로필 없는 role → `activeProfileId=null`, `@CurrentXxx` 리졸버 깨짐 |
| 웹 role 릴레이를 `state` 에 실어 보내기 | Apple 은 `state` 를 CSRF 검증에, Google 은 Spring Security 가 authorization request 로 관리 → 끼워넣기 지저분. **별도 `signup_role` 쿠키**가 구글·애플 공통으로 단순 |
| 리프레시는 role 없이 recentRole 로 발급 유지 | recentRole 은 audit 용으로만 두기로 함. 프론트가 어느 모드로 갈지 항상 알므로 명시받는 게 명확 |
| role 전용 enum 신설 | `MemberRole { CUSTOMER, ARTIST }` 로 충분(switchRole 도 재사용) |

## 결과 (트레이드오프)
- ✅ 신규 유저 토큰 정합성 보장(프로필 없는 role 을 activeRole 로 안 박음).
- ✅ activeRole 결정 규칙이 단일화(override 우선) → recentRole 이중 소스 제거로 추론 쉬움.
- ✅ 네이티브·웹·리프레시가 role 처리 일관.
- ⚠️ 웹은 `signup_role` 쿠키가 SameSite=None; Secure 라 **https 환경 전제**(Apple state 쿠키와 동일 제약).
- ⚠️ 온보딩/전환/리프레시 응답의 **새 accessToken 을 프론트가 즉시 교체**해야 하는 계약이 늘어남(스웨거에 명시).
- ⚠️ `recentRole` 은 저장은 되나 로직에서 안 쓰는 "관찰용" 컬럼이 됨 → 용도 오해 방지 주석 필요.

## 관련
- `domain/auth/dto/{NativeLoginRequest,SocialLoginResponse,TokenRefreshRequest}`
- `domain/auth/service/AuthService#resolveActiveRoleForLogin`, `#issueTokens(…, overrideActiveRole)`
- `domain/auth/facade/AuthFacade#nativeLogin`, `global/auth/oauth/SignupRoleCookie`
- `global/auth/oauth/custom/{AppleWebLoginService,GoogleWebLoginService}`, `global/auth/oauth/OAuth2SuccessHandler`
- `global/auth/jwt/TokenProvider#makeToken` (recentRole 제거)
- `domain/member/facade/MemberFacade#refresh(role)`, `domain/member/controller/MemberController`

## 관련 노트
- [[소셜 로그인 요청에 고객·작가 Role 선택 포함]]
- [[ADR-004 소셜 로그인 - 구글·애플 네이티브·웹]]
- [[ADR-003 Party-Role 다중역할 모델링 + 역할 전환]]
- [[ADR-006 역할별 인증 파라미터 리졸버 3종 설계]]
