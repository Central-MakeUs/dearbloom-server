> **ADR-017 · Auth · Accepted · 2026-08-09**
> 카톡으로 공유하는 공동보드 초대 링크를 어떻게 식별할지 결정. → **URL 에 PK 를 싣지 않고 추측 불가능한 초대 코드로 보드를 식별한다. 미리보기는 비로그인도 볼 수 있게 열되(카톡 OG 크롤러 때문에 필수) 보드 내부 정보는 담지 않고, 입장은 멱등하게 만든다.**

## 맥락
- 초대 방식이 "코드 입력"이 아니라 **URL 클릭**이다. 카톡으로 링크를 던지면 친구가 눌러 들어온다.
- 초기 입장 API 는 `POST /api/shared-boards/{sharedBoardId}/members` 였다. **`sharedBoardId` 는 `IDENTITY` 순차 증가**라, 로그인만 했으면 1, 2, 3… 을 차례로 호출해 **남의 보드에 전부 입장**할 수 있었다(추측이 아니라 그냥 세는 것). 초대 링크와 무관하게 이미 뚫려 있던 구멍.
- 시안의 진입 화면은 **비로그인 상태에서도** 보드명·방장명을 보여준다.
- 카톡에 링크를 붙였을 때 미리보기 카드가 뜨려면 OG 태그가 필요한데, **카카오 크롤러는 토큰이 없다.**
- 링크는 카톡방에 남아 **이미 참여한 사람이 다시 누르는 일이 흔하다.**

## 결정
- **`SharedBoard.inviteCode` 로 식별** — `unique`, `nullable=false`. URL 은 `/app/boards/invite/{inviteCode}`.
- **코드 공간은 약 8.9억** — 대문자·숫자에서 혼동 문자(`0 O 1 I L`)를 뺀 31자 중 6자, `SecureRandom`. 보드 생성 시 발급하고 중복이면 최대 5회 재생성(DB unique 가 최종 방어선).
- **기존 `POST /{sharedBoardId}/members` 제거.** 입장도 코드 기반(`POST /invite/{inviteCode}/members`)으로 바꿔 코드를 가진 사람만 들어오게 한다.
- **미리보기 `GET /invite/{inviteCode}` 는 비로그인 허용** — `PublicPaths.OPTIONAL_AUTH_PREFIXES` 에 등록. **`SKIP_TOKEN_PREFIXES` 가 아니다** — 스킵하면 SecurityContext 가 비어 로그인한 사용자도 게스트로 취급되고 `alreadyJoined` 가 항상 false 가 된다. `@CurrentViewer` 로 받는다([[ADR-006 역할별 인증 파라미터 리졸버 3종 설계]]).
- **미리보기 응답에 보드 내부 정보를 담지 않는다** — 보드명 / 방장명 / 인원수 / `alreadyJoined` 까지만. 작품·댓글·멤버 목록은 참여 후에만.
- **`alreadyJoined` 로 프론트가 화면을 3분기** — 비로그인이면 로그인 유도, 미참여면 참여 버튼, 이미 참여면 곧바로 보드로.
- **입장은 멱등** — 이미 멤버여도 성공(201)으로 응답하고 보드 정보를 돌려준다. 409 를 던지면 링크 재클릭이 에러 화면으로 끝난다.
- **초대 코드 조회는 별도 API**(`GET /{sharedBoardId}/invite-code`, **참여 멤버만**). 보드 목록·생성 응답에는 싣지 않는다.
- **초대는 멤버 누구나.** 방장 전용이 아니다.
- 공유 링크 조립(`{origin}/app/boards/invite/{code}`)은 프론트가 한다.

## 검토한 대안
| 대안 | 기각 이유 |
|---|---|
| URL 에 `sharedBoardId` 를 그대로 | 순차 증가 PK 라 훑으면 남의 보드가 전부 열린다 |
| 숫자 6자리 코드 | 100만 조합. 활성 보드가 1만 개면 아무 코드나 찍어도 1% 확률로 적중. 무작위 대입에 무너진다 |
| 미리보기를 로그인 필수로 | **카톡 미리보기 카드가 안 뜬다**(크롤러에 토큰이 없다). 비로그인 진입 화면도 못 그린다 |
| 미리보기를 `SKIP_TOKEN_PREFIXES` 에 등록 | 토큰을 아예 안 읽어 로그인 사용자도 게스트가 된다 → `alreadyJoined` 가 항상 false |
| 초대 코드를 `SharedMember` 에 달아 초대자별 링크 | 기능은 더 좋지만(누가 초대했는지 표시) **초대자가 탈퇴하면 뿌려둔 링크가 전부 죽는다.** 그 처리를 정하는 비용이 이득보다 큼 |
| 초대자를 쿼리 파라미터로(`?from=...`) | **위조된다.** 아무나 바꿔서 다른 사람이 초대한 것처럼 보이게 할 수 있고, 공유 과정에서 잘리기도 한다 |
| 초대 코드를 보드 목록·생성 응답에 포함 | 코드가 곧 입장 권한이다. 여러 응답을 타고 퍼지면 통제 지점이 사라진다 |
| 이미 참여 중이면 409 | 카톡에 남은 링크 재클릭이 정상 흐름인데 에러 화면으로 끝난다 |
| 서버가 완성된 `inviteUrl` 을 내려줌 | 프론트 도메인을 서버가 알아야 한다. 링크 조립은 프론트 책임으로 둠 |

## 결과 (트레이드오프)
- ✅ **PK 훑기로 남의 보드에 입장하던 구멍이 닫힌다.** 코드 없이는 미리보기도 입장도 불가.
- ✅ 카톡 OG 미리보기가 가능하고, 비로그인 진입 화면을 서버 렌더링으로 그릴 수 있다.
- ✅ 링크를 몇 번을 눌러도 같은 결과 — 재클릭·중복 탭이 안전하다.
- ✅ 코드 노출 경로가 `GET /{id}/invite-code` 하나로 좁혀져, 나중에 재발급·비활성화를 걸 지점이 명확하다.
- ⚠️ **코드를 아는 사람은 누구나 들어온다.** 링크가 단톡방에 재공유되면 막을 수단이 없다 — 정원 상한·재발급·강퇴가 실질 통제 수단인데 아직 없다.
- ⚠️ **방장 이름이 비로그인에게 실명으로 노출된다.** `Customer.name` 은 실명(`@ValidRealName`)이라 코드를 훑으면 실명 수집이 가능하다. 마스킹(`김*어`)을 검토했으나 시안대로 우선 두었다.
- ⚠️ **만료가 없다.** 오래된 카톡방·스크린샷에 남은 코드가 영구히 유효하다. 7일 만료 + 방장 재발급이 후속 과제.
- ⚠️ 시도 횟수 제한이 없다. 8.9억 조합이라 당장 위험은 낮지만, 계정/IP 당 rate limit 이 실질 방어선이다.
- ⚠️ `inviteCode` 가 `nullable=false` 라 **기존 보드 행이 있으면 스키마 변경이 실패한다.** 배포 전 백필 확인 필요.

## 관련
- `domain/board/entity/board/SharedBoard#inviteCode`, `domain/board/util/InviteCodeGenerator`
- `domain/board/controller/SharedMemberController` — `GET /invite/{code}`, `POST /invite/{code}/members`, `GET /{id}/invite-code`
- `domain/board/facade/SharedMemberFacade#{getInvite, joinByInviteCode, getInviteCode}`
- `global/auth/jwt/PublicPaths#OPTIONAL_AUTH_PREFIXES`

## 관련 노트
- [[ADR-016 공동보드 - 참여자별 공유작품 행과 보드 단위 댓글]]
- [[ADR-006 역할별 인증 파라미터 리졸버 3종 설계]]
- [[ADR-005 작품 조회 뷰어별 분기 전략]] — 옵셔널 인증을 쓰는 같은 이유
