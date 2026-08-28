> **ADR-003 · Domain · Accepted · 2026-07-02 ~ 2026-07-13**
> 한 사람이 고객·작가 역할을 동시에 가질 수 있는 도메인을 어떻게 모델링할지 결정. → **단일 Member + 역할 플래그 + 역할별 프로필 엔티티(Party-Role 패턴), 토큰 기반 역할 전환.**

## 맥락
- 한 사용자가 **고객이면서 작가**일 수 있다(작가도 남의 작품을 저장·문의).
- 같은 계정/로그인으로 두 역할을 오가야 하고, API 는 "지금 어떤 역할로 접속했는지"를 알아야 한다.

## 결정
- **단일 `Member`** 엔티티에 `hasCustomer`/`hasArtist` 플래그 + `recentRole`.
- 역할별 데이터는 **`Customer`·`Artist` 엔티티**로 분리, 각 `Member` 와 1:1.
- 토큰 클레임에 **`activeRole`(현재 역할) + `activeProfileId`(그 역할의 PK)** 를 실어, 요청이 어느 역할로 들어왔는지 서버가 판별.
- **역할 전환** `PATCH /api/members/me/role`: 대상 role 프로필 보유 검증 후 `activeRole` 갱신된 새 accessToken 발급(refresh 는 그대로).
- **`@CurrentCustomer`/`@CurrentArtist`** 리졸버가 `activeProfileId` 로 프로필을 주입, role 불일치 시 403. (조회 공용은 `@CurrentViewer` → [[역할별 인증 파라미터 리졸버 3종 설계]])

## 검토한 대안
| 대안 | 기각 이유 |
|---|---|
| 역할별 별도 User 테이블(고객 계정/작가 계정 분리) | 같은 사람이 두 번 가입, 계정 연결/중복 로그인 문제 |
| 단일 User 에 role enum 하나만 | 겸업 표현 불가(고객 ∧ 작가 동시) |
| 단일 테이블에 고객·작가 컬럼 모두 | 역할별 필드가 섞여 응집도 낮고 nullable 폭증 |

## 결과 (트레이드오프)
- ✅ 한 계정으로 두 역할, **재로그인 없이 토큰만 교체해 전환**.
- ✅ 역할별 데이터는 각 엔티티로 깔끔히 분리, 리졸버로 접근 제어 일원화.
- ⚠️ 토큰이 `activeProfileId` 를 들고 다녀야 하고, 전환 시 토큰 재발급 필요.
- ⚠️ 리졸버가 주입한 엔티티는 detached → LAZY/쓰기 함정 존재 → [[리졸버 주입 엔티티의 detached·LazyInit 함정]]

## 관련
- `domain/member/entity/Member`(hasCustomer/hasArtist/recentRole), `Customer`/`Artist`, `MemberController#switchRole`, `global/auth/jwt/TokenProvider`(activeRole/activeProfileId)

## 관련 노트
- [[역할별 인증 파라미터 리졸버 3종 설계]]
- [[소셜 로그인 시 고객·작가 역할 선택 전달]]
