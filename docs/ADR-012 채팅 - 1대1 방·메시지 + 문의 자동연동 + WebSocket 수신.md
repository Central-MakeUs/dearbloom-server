> **ADR-012 · Domain · Accepted · 2026-07-24**
> 고객↔작가 1:1 채팅을 어떻게 둘지 결정. → **방은 (고객, 작가) 프로필 쌍당 unique, 문의 생성 시 동기 이벤트로 방 자동 생성 + 문의 카드(inquiry_id 참조) append, 메시지는 타입 배타(TEXT/IMAGE/INQUIRY), 쓰기는 REST·수신은 WebSocket(STOMP /topic) 브로드캐스트, 안읽음·미리보기는 방에 비정규화. MySQL 먼저, Redis 나중.**

## 맥락
- 문의를 신청하면 그 고객·작가 사이 채팅방이 자동 생성되고, 이미 있으면 재사용하며 문의 내용만 카드로 전달된다(기획).
- 채팅 화면 메시지가 두 성격 — 일반 텍스트(+이미지)와 **문의 카드**(패키지명·작가·촬영일시·학교·인원·요청사항 + 작품상세 보기).
- Member 는 프로필 최대 2개(고객/작가) 보유([[ADR-003 Party-Role 다중역할 모델링 + 역할 전환]]) → 방을 member 로 잡으면 "A가 B의 고객 / B가 A의 고객" 두 관계가 뭉갠다.
- 문의는 이미 스냅샷 기반([[ADR-009 스마트 문의·예약 - 상태 단일화와 슬롯 잠금]]) → 카드가 값을 다시 복사할 필요 없음.
- 저사양 단일 인스턴스([[ADR-001 저사양 개발 서버 리소스 대응 전략]]) → 지금은 Redis/외부 브로커 없이 인메모리 브로커로 충분.
- 지금은 MySQL 만, Redis 캐싱은 후속.

## 결정
- **방 키 = (customer_id, artist_id) UNIQUE.** 프로필 쌍당 1개. 역할이 뒤바뀐 관계는 별개의 방. 목록용으로 `last_message_preview`·`last_message_at`·`customer/artist_unread`·`customer/artist_last_read_at` 를 방에 **비정규화**(목록 O(1), Redis 이관 친화).
- **문의 → 채팅 자동 연동(동기 이벤트).** `CustomerInquiryFacade.createInquiry` 가 `InquiryCreatedEvent` 발행 → chat 리스너가 같은 트랜잭션에서 방 find-or-create + `message_type=INQUIRY` 카드(발신=고객, `inquiry_id` 참조) append. 문의 도메인은 chat 을 import 하지 않는다(디커플). 카드는 문의 스냅샷에서 렌더, `작품상세 보기`는 `inquiry.artworkPackage.artwork.artworkId`.
- **메시지 타입 배타** — `ChatMessageType {TEXT, IMAGE, INQUIRY}`. 한 메시지는 `content`(TEXT) / `image_url`(IMAGE) / `inquiry_id`(INQUIRY) 중 하나만 채운다. 이미지는 **한 장씩**, 텍스트와 **동시 전송 불가**(엔드포인트 분리). 업로드는 기존 presigned/S3/CDN 재사용(prefix=CHAT_IMAGE), 서버는 CDN URL 검증만.
- **쓰기=REST, 수신=WebSocket.** 전송은 `POST /api/chat/rooms/{id}/messages|images` 로 저장(기존 JWT 필터·검증 재사용) 후 서버가 `SimpMessagingTemplate` 로 `/topic/rooms/{id}` 브로드캐스트. STOMP inbound 메시지 매핑 인증을 안 만들어도 됨.
- **STOMP 인가** — `StompAuthChannelInterceptor`: CONNECT 에서 `Authorization: Bearer` 토큰 검증 → (memberId, activeRole, profileId) Principal, SUBSCRIBE `/topic/rooms/{id}` 는 참여자만(프로젝션으로 참여자 PK만 조회, 트랜잭션 밖 LAZY 회피). 타인 방 도청 차단.
- **접근 주체 판별** — `@CurrentChatParticipant` 리졸버가 토큰 activeRole+activeProfileId 로 `ChatParticipant(role, profileId)` 주입. 고객/작가 대칭이라 목록/전송/읽음이 단일 엔드포인트. `sender_role` 은 `MemberRole` 재사용.
- **읽음 = 명시 호출.** `POST /rooms/{id}/read`(내 안읽음 0). 방 진입/수신 시 프론트가 호출. presence 기반 자동 읽음은 미구현(후속).

## 검토한 대안
| 대안 | 기각 이유 |
|---|---|
| 방을 member 쌍으로 | dual-role 에서 "A고객↔B작가"와 "B고객↔A작가"가 뭉갬. 프로필 쌍이 맞음 |
| 카드가 문의 필드를 재스냅샷 | Inquiry 가 이미 스냅샷 보유 → 중복. `inquiry_id` 참조로 충분 |
| inquiry 파사드가 chat 직접 호출 | 도메인 컴파일 결합. 동기 이벤트로 디커플(같은 tx 원자성 유지) |
| 전송도 STOMP(@MessageMapping) | 인증·검증을 WS 쪽에 또 구현. REST 쓰기면 기존 필터/밸리데이션 재사용 |
| 폴링 | 저사양 서버에 낭비. WS 수신이 효율적 |
| 안읽음 COUNT 쿼리(방마다) | 목록 N+1. 방 비정규화 카운터로 O(1) |
| 텍스트+이미지 한 메시지 | 타입 배타 + 엔드포인트 분리로 "동시 전송 불가" 구조 강제 |
| presence 자동 읽음 | 구독 추적 필요 → 후속. 지금은 프론트 주도 read |

## 결과 (트레이드오프)
- ✅ 목록 O(1)(비정규화), 문의+방+카드 원자적(같은 tx), 도메인 디커플(이벤트), 구독 도청 차단.
- ✅ 쓰기 REST 라 인증·검증·에러처리를 기존 스택 그대로 사용, 이미지는 enum+컬럼 1개로 확장.
- ⚠️ 브로드캐스트를 **트랜잭션 안**에서 수행 — 이후 롤백 시 유령 push 가능(창 좁음, MVP 허용. 필요 시 afterCommit 로 이전).
- ⚠️ **SimpleBroker(인메모리)** — 다중 인스턴스면 Redis pub/sub·외부 STOMP 릴레이 필요.
- ⚠️ 비정규화 카운터는 동시 편집에 드리프트 여지(메시지당 단일 writer 라 실무상 안전).
- ⚠️ 읽음이 프론트 주도(진입+수신 시 read 호출). "방에 있으면 즉시 읽음"은 presence 붙여야 서버 자동화.
- ⚠️ 동일 쌍 동시 첫 문의(희귀)는 unique 위반으로 롤백→재시도.

## Redis 이관 경로 (후속)
- 안읽음/미리보기 카운터 → Redis(HINCRBY), 목록 정렬 → Sorted Set(room by time), 실시간 팬아웃 → Redis pub/sub 릴레이. 스키마는 비정규화라 캐시만 얹으면 됨(변경 없음).

## 관련
- `domain/chat/entity/{ChatRoom, ChatMessage, ChatMessageType}`
- `domain/chat/repository/{ChatRoomRepository(findParticipants 프로젝션), ChatMessageRepository(커서 히스토리)}`
- `domain/chat/service/{ChatRoom·ChatMessage Command/Query, ChatEventPublisher}`, `facade/ChatFacade`, `controller/ChatController`
- `domain/chat/ws/{StompAuthChannelInterceptor, ChatPrincipal}`, `global/config/WebSocketConfig`
- `domain/chat/event/ChatInquiryEventListener` ← `domain/inquiry/event/InquiryCreatedEvent`(발행: CustomerInquiryFacade)
- `global/auth/resolver/CurrentChatParticipant(+ArgumentResolver)`, `global/file/dto/FilePrefix(CHAT_IMAGE)`

## 관련 노트
- [[ADR-009 스마트 문의·예약 - 상태 단일화와 슬롯 잠금]]
- [[ADR-003 Party-Role 다중역할 모델링 + 역할 전환]]
- [[ADR-001 저사양 개발 서버 리소스 대응 전략]]
- [[ADR-006 역할별 인증 파라미터 리졸버 3종 설계]]
