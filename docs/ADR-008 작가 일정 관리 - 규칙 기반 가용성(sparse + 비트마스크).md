> **ADR-008 · Scheduling · Accepted · 2026-07-23**
> 작가 촬영 가용성을 어떻게 저장·계산할지 결정. → **셀을 저장하지 않고 규칙(sparse)만 저장, 조회 때 09:00~21:00 고정 그리드의 24비트 마스크로 합성. 예약 확정 슬롯은 별도 테이블 없이 RESERVED 문의에서 계산.**

## 맥락
- 고객 스마트 문의에서 작가의 날짜·시간 가용성을 보여줘야 함. 시간축은 **09:00~21:00 고정, 30분 단위 = 하루 24칸**.
- 가용성은 3종 규칙의 합성이다: **기본 촬영 가능**(요일 반복) − **반복 예약 불가**(요일 반복) − **개인 예약 불가**(특정 날짜) − **예약 확정**(booked).
- 초기 스케치(ADR.md "빠진 후보")는 *dense TimeSlot 캐시 + DB unique 제약*을 가정했으나, 실제 요구(고정 그리드·읽기 위주·단일 작가 액션)를 보고 재설계함.

## 결정
- **가용성은 규칙만 저장(sparse)** — `ArtistScheduleRule(rule_type ∈ {WEEKLY_AVAILABLE, WEEKLY_BLOCK, DATE_BLOCK})` 단일 테이블. 셀을 날짜별로 materialize 하지 않는다.
- **조회 시 비트마스크 합성** — `SlotGrid`(OPEN=09:00, 24칸, `FULL_MASK=(1<<24)-1`). `available = base & ~block & ~booked & FULL_MASK`. 요일 매칭·마스크 연산은 애플리케이션 레벨.
- **예약 오픈 창** `BookingWindow` — 오늘 ~ 오늘+3개월. 창 밖·과거 날짜는 규칙과 무관하게 마스크 0.
- **예약 확정 슬롯도 별도 테이블 없음** — `BookedSlotProvider`(artist 도메인 port) ← `InquiryBookedSlotProvider`(inquiry 도메인 adapter)가 **status=RESERVED 문의**를 읽어 booked 마스크 계산. 예약 완료=RESERVED로 자동 잠김, 취소 시 자동 해제.
- **N+1 회피** — 캘린더 조회는 요일 규칙 1쿼리 + 기간 date-block 1쿼리 + booked 배치 1쿼리로 끝내고 날짜별 루프는 메모리 계산.
- 패키지 소요시간만큼 **연속 셀** 예약 가능 여부는 `startableMask`(우측 시프트 AND)로 계산, 문의 전송 시 서버 재검증.

## 검토한 대안
| 대안 | 기각 이유 |
|---|---|
| dense 저장(셀마다 행, 옛 `TimeSlot`) | `24 × 365 × 작가수` 행. 기본 시간 한 번 바꾸면 수천 행 재생성. 규칙이면 O(규칙 수). → `TimeSlot`/`TimeSlotStatus` 폐기 |
| 예약 확정 셀 unique 잠금 테이블(초기 가정) | 예약 완료는 **단일 액터(작가)** 라 동시성 낮음. status=RESERVED 로 booked 계산하면 테이블·삭제 로직 없이 "예약완료=잠김/취소=열림"이 공짜. |
| 규칙 2테이블 분리(weekly/date) | 3-type 단일 테이블이 CRUD 하나로 단순. |
| LocalTime 대신 slot index 저장 | 저장은 사람이 읽기 쉬운 LocalTime, 계산만 인덱스/마스크로 변환(SlotGrid) 하는 게 균형 좋음. |

## 결과 (트레이드오프)
- ✅ 저장량 O(규칙) + O(booked=RESERVED 문의). 매우 가벼움.
- ✅ 가용성 계산이 규칙 몇 행 + 24비트 정수 연산 → 캐시 없이도 저렴.
- ✅ 작가 캘린더·고객 문의 조회·문의 전송 검증이 **한 계산(`ScheduleAvailabilityService`)** 공유 → 화면 간 불일치 없음.
- ✅ port/adapter 로 artist↔inquiry 도메인 순환 없이 booked 반영.
- ⚠️ booked 를 status=RESERVED 로 계산 → 예약 완료 동시성은 **check-then-set**(단일 작가라 실무상 안전, 하드 DB 잠금 아님). 멀티 액터로 커지면 unique 잠금 테이블 재도입 여지.
- ⚠️ 09:00~21:00·30분·3개월이 **하드코딩 상수**(SlotGrid/BookingWindow). 정책 바뀌면 상수 수정 필요.
- ⚠️ 캘린더 booked 계산이 RESERVED 문의를 조회 → 트래픽 커지면 Redis 캐시 도입 지점(창이 날짜 상대적이라 TTL 필수).

## 관련
- `domain/artist/entity/schedule/{ArtistScheduleRule,ScheduleRuleType}`
- `domain/artist/util/{SlotGrid,BookingWindow}`
- `domain/artist/service/schedule/{ScheduleAvailabilityService,ScheduleCommandService,ScheduleQueryService}`, `BookedSlotProvider`
- `domain/inquiry/service/InquiryBookedSlotProvider` (adapter)
- `domain/artist/controller/ScheduleController` (`/api/artists/me/schedule`)

## 관련 노트
- [[ADR-005 작품 조회 뷰어별 분기 전략]]
- [[역할별 인증 파라미터 리졸버 3종 설계]]
