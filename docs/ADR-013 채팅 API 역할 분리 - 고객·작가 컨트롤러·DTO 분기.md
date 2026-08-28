> **ADR-013 · Layering · Accepted · 2026-07-30**
> 채팅 API 를 고객/작가 대칭 단일 엔드포인트로 둘지 역할별로 쪼갤지 재결정. → **역할별 URL·컨트롤러 2개(`/api/customers/me/chat`, `/api/artists/me/chat`)로 분리하고 응답 DTO 도 역할별로 분기. 상대방 필드는 `counterpartyXxx` 대신 각 역할 관점의 구체 이름(`artistNickname` / `customerName`)으로 확정. `@CurrentChatParticipant` 리졸버는 제거하고 `@CurrentCustomer`/`@CurrentArtist` 로 대체. 전송·읽음은 동작이 같아 파사드에서 `role` 파라미터로 공용 처리, WebSocket 브로드캐스트 페이로드는 공용 유지.**
>
> [[ADR-012 채팅 - 1대1 방·메시지 + 문의 자동연동 + WebSocket 수신]] 의 "접근 주체 판별 = 단일 엔드포인트 + `@CurrentChatParticipant`" 결정을 **supersede** 한다. 그 외(방 키, 문의 자동연동, 타입 배타, 쓰기 REST/수신 WS, 비정규화 카운터)는 그대로 유효.

## 맥락
- ADR-012 는 "채팅은 고객/작가 대칭"이라는 전제로 목록·히스토리·전송·읽음을 **단일 엔드포인트**(`/api/chat/**`)에 두고, 토큰 `activeRole` 로 내 편을 판별했다(`ChatParticipant`).
- 그런데 화면 요구가 갈리기 시작했다. 고객 화면은 상대가 항상 **작가**(닉네임·프로필 이미지), 작가 화면은 상대가 항상 **고객**(이름·프로필 이미지) — 대칭이 아니라 **각자 다른 필드**가 필요하다.
- 단일 DTO 로 버티려면 `counterpartyName`/`counterpartyImageUrl` 같은 중립 이름 + `viewerRole` 분기가 필요한데, 이러면 (a) 프론트가 필드만 보고 뭘 받는지 모르고 (b) Swagger 스키마 하나에 두 역할 설명이 섞이고 (c) 한쪽만 필요한 필드(작가 이미지 등)가 다른 쪽에서 항상 `null` 로 나간다. 실제로 기존 `ArtistChatRoomSummaryResponse` 는 `counterpartyImageUrl` 을 `null` 하드코딩하고 있었다.
- 문의 도메인엔 이미 역할별 분리 선례가 있다 — `CustomerInquiryController`/`ArtistInquiryController`, `dto/response/customer/*`·`dto/response/artist/*`. 채팅만 대칭 단일 구조로 남으면 도메인 간 규칙이 어긋난다.
- 경로에 역할이 박히면 토큰 `activeRole` 로 역할을 **판별할** 이유가 사라진다(경로가 이미 역할을 고정하고, 리졸버가 프로필 존재까지 검증).

## 결정
- **역할별 컨트롤러 2개.** `CustomerChatController`(`/api/customers/me/chat`), `ArtistChatController`(`/api/artists/me/chat`). 각각 5개 — `GET /rooms`, `GET /rooms/{roomId}/messages`, `POST /rooms/{roomId}/messages`, `POST /rooms/{roomId}/images`, `POST /rooms/{roomId}/read`. 문의 도메인의 `/api/artists/me/inquiries` 패턴과 정렬.
- **인증 주체 = `@CurrentCustomer` / `@CurrentArtist`.** `ChatParticipant`·`@CurrentChatParticipant`·`CurrentChatParticipantArgumentResolver` 삭제, `WebMvcConfig` 등록도 제거. 경로가 역할을 고정하므로 리졸버 3종([[ADR-006 역할별 인증 파라미터 리졸버 3종 설계]])으로 충분하다.
- **응답 DTO 역할별 4개.** `dto/response/customer/{CustomerChatRoomSummaryResponse, CustomerChatMessageResponse}`, `dto/response/artist/{ArtistChatRoomSummaryResponse, ArtistChatMessageResponse}`. 공용 `ChatRoomSummaryResponse` 는 참조 0 이 되어 삭제. **상대방 필드는 구체 이름으로 확정** — 고객 응답은 `artistNickname`·`artistImageUrl`, 작가 응답은 `customerName`·`customerImageUrl`. `viewerRole` 분기 없이 각 DTO 가 `of(room)` 하나만 갖는다.
- **파사드 분리 기준 = 반환 타입.** 조회는 응답 타입이 갈리므로 역할별 메서드(`getCustomerRooms`/`getArtistRooms`, `getCustomerMessages`/`getArtistMessages`), 전송·읽음은 동작이 **완전히 동일**하므로 `sendText(role, profileId, ...)` 처럼 `role` 만 파라미터로 받아 공용 처리. 컨트롤러가 자기 역할을 상수로 넘긴다.
- **메시지 DTO 에 상대 정보를 건마다 싣는다.** 상대 프로필은 방 단위 값이라 히스토리 N건에 같은 값이 N번 반복되지만, 말풍선 옆 프로필 렌더에 그대로 쓰기 위해 봉투(`{artist..., messages:[...]}`) 대신 이 형태를 택했다. 파사드가 권한 검증에서 이미 `ChatRoom` 을 들고 있어 **추가 쿼리 없음**(상대 프로필은 LAZY 1회 로드, 메시지 수와 무관).
- **`senderRole` 유지.** 고정된 것은 보는 사람(viewer)의 역할이고 발신자는 메시지마다 다르다(히스토리에 양쪽 메시지가 섞임). 좌/우 말풍선 구분에 필요.
- **`ChatMessageResponse` 는 공용 유지.** 전송 응답 + WebSocket `/topic/rooms/{id}` 브로드캐스트가 함께 쓴다. 브로드캐스트는 한 토픽으로 양쪽 구독자에게 같은 페이로드가 나가므로 역할별로 쪼갤 수 없다.
- **고객 프로필 이미지 도입.** `Customer.imageUrl` 필드 추가 + 기본 이미지 4종 enum `CustomerProfileImage {GREEN, GREY, BROWN, BLUE}` 준비(직접 업로드 없이 이 중 택1). **아직 미배선** — 가입 시 배당·응답 반영은 후속.

## 검토한 대안
| 대안 | 기각 이유 |
|---|---|
| 단일 엔드포인트 + `viewerRole` 분기 유지(ADR-012) | 상대방 필드가 역할마다 달라 중립 이름(`counterpartyXxx`)이 강제되고, 한쪽만 쓰는 필드가 상대편에서 항상 null. Swagger 스키마도 두 역할이 섞임 |
| Command/Query 컨트롤러 분리(`ChatQueryController`/`ChatCommandController`) | 실제로 만들어봤으나 기각. 갈리는 축은 CQ 가 아니라 **역할**이다. 조회만 역할별로 쪼개면 전송·읽음이 공용 `/api/chat` 에 남아 한 도메인에 URL 체계가 두 개 생긴다 |
| 고객은 `/api/chat` 유지, 작가만 `/api/artists/me/chat` | 문의 도메인 선례(고객=무수식 경로)와는 맞지만 채팅은 좌우 대칭 기능이라 비대칭 URL 이 오히려 헷갈림. 프론트 수정 최소화보다 일관성 우선 |
| `/api/chat/customer`, `/api/chat/artist` | chat 하위에 모이는 건 장점이나 기존 `/api/{role}s/me/**` 관례에서 벗어남 |
| 전송·읽음도 역할별 파사드 메서드로 완전 분리 | 동작·반환 타입이 동일해 순수 중복. `role` 파라미터 하나로 충분 |
| 상대 정보를 히스토리 봉투에 1회만 | 페이로드는 가벼워지나 말풍선별 프로필 렌더에서 프론트가 매번 상위 객체를 참조해야 함. 렌더 편의 우선 |
| 전송 응답도 역할별 DTO 로 | 같은 저장 결과를 WS 브로드캐스트와 공유하므로 역할별 분기 불가(한 토픽·한 페이로드) |
| `Customer.imageUrl` 을 자유 URL(문자열)로 운용 | 기본 이미지 4종 고정이면 enum 이 값 범위를 강제. 다만 이번엔 enum 만 준비하고 전환은 후속으로 미룸 |

## 결과 (트레이드오프)
- ✅ 프론트가 URL·스키마만 보고 자기 역할 응답을 특정. `null` 자리 필드가 사라지고 Swagger 가 역할별로 갈림.
- ✅ 문의 도메인과 규칙 정렬(컨트롤러·DTO 패키지·경로 모두). `ChatParticipant`/전용 리졸버가 사라져 인증 진입점이 리졸버 3종으로 단순화.
- ✅ 조회 시 추가 쿼리 없음(권한 검증에서 얻은 `ChatRoom` 재사용).
- ⚠️ **엔드포인트 수가 2배**(5 → 10). 전송·읽음은 컨트롤러 코드가 역할별로 중복(파사드는 공용이라 로직 중복은 없음).
- ⚠️ **역할별 REST + 공용 WS** 의 비대칭 — 같은 메시지를 REST 응답은 역할별 DTO, WS 푸시는 공용 `ChatMessageResponse` 로 받는다. 프론트가 두 형태를 함께 다뤄야 함.
- ⚠️ 히스토리 응답에 상대 프로필이 메시지마다 중복(30건이면 30회). 페이로드 낭비를 렌더 편의와 교환.
- ⚠️ 고객 프로필 이미지가 **미완**: `Customer.imageUrl` 컬럼과 enum 만 있고 값을 채우는 경로가 없어 당장은 계속 `null`. 또 `Customer.anonymize()` 가 `name`·`university` 만 비우고 `imageUrl` 은 남겨 탈퇴 후에도 사진이 노출된다(후속 정리 필요).
- ⚠️ ADR-005([[ADR-005 작품 조회 뷰어별 분기 전략]])는 반대로 **단일 엔드포인트 + 뷰어별 응답**을 택했다 — 상충이 아니라 조건 차이다. 작품 조회는 **비로그인**까지 3분기라 URL 로 쪼개면 익명 경로가 붕 뜨고 응답 차이도 필드 가감 수준이지만, 채팅은 로그인·프로필 보유가 전제인 2분기이고 상대방 필드 자체가 다르다. **"뷰어에 비로그인이 섞이면 단일 + 분기, 역할이 전제이고 응답 골격이 다르면 역할별 URL"** 을 기준으로 삼는다.

## 함께 정리한 것 (문의 응답)
- `CustomerInquiryDetailResponse`·`ArtistInquiryDetailResponse` 에 `artworkId` 추가(작품 상세 이동용). 다른 필드는 스냅샷인데 이것만 실시간 FK 참조라, 작품이 삭제되면 이동 시 404 가능 — 필요하면 `artworkIdSnapshot` 으로 전환.
- `ArtistInquirySummaryResponse`·`ArtistInquiryDetailResponse` 에 `customerName`, 목록엔 `customerImageUrl` 까지 추가(작가가 누가 문의했는지 목록에서 식별).
- 작가 문의 목록 쿼리에 `join fetch i.customer` 추가 — `Inquiry.customer` 가 LAZY 라 고객명 참조 시 행마다 쿼리가 나가는 N+1 이 생겼다.

## 관련
- `domain/chat/controller/{CustomerChatController, ArtistChatController}` (기존 `ChatController` 대체)
- `domain/chat/dto/response/customer/{CustomerChatRoomSummaryResponse, CustomerChatMessageResponse}`
- `domain/chat/dto/response/artist/{ArtistChatRoomSummaryResponse, ArtistChatMessageResponse}`
- `domain/chat/dto/response/{ChatMessageResponse(공용·WS), InquiryCardResponse}` / 삭제: `ChatRoomSummaryResponse`, `dto/ChatParticipant`
- `domain/chat/facade/ChatFacade` — 조회는 역할별 메서드, 전송·읽음은 `role` 파라미터 공용
- 삭제: `global/auth/resolver/CurrentChatParticipant(+ArgumentResolver)`, `global/config/WebMvcConfig` 등록 제거
- `domain/customer/entity/{Customer(imageUrl), CustomerProfileImage}`
- `domain/inquiry/dto/response/{customer, artist}/*`, `domain/inquiry/repository/InquiryRepository`(join fetch customer)

## 관련 노트
- [[ADR-012 채팅 - 1대1 방·메시지 + 문의 자동연동 + WebSocket 수신]] (일부 supersede)
- [[ADR-006 역할별 인증 파라미터 리졸버 3종 설계]]
- [[ADR-005 작품 조회 뷰어별 분기 전략]]
- [[ADR-009 스마트 문의·예약 - 상태 단일화와 슬롯 잠금]]
- [[ADR-003 Party-Role 다중역할 모델링 + 역할 전환]]
