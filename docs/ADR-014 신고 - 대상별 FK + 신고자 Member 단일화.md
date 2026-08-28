> **ADR-014 · Domain · Accepted · 2026-08-09**
> 신고 대상이 앞으로 늘어날 때(작품 → 채팅 메시지 → …) 테이블을 어떻게 둘지 결정. → **한 `Report` 테이블에 `targetType` + 대상별 nullable FK 를 두고 그중 하나만 채운다. 반면 신고자는 역할별로 쪼개지 않고 `Member` 하나로 합친다.**

## 맥락
- 작품 신고로 시작했지만 채팅 메시지 신고가 곧 필요하다. 대상 종류가 계속 늘어난다.
- 신고자는 고객일 수도 작가일 수도 있다. 고객→작가 / 작가→고객 양방향.
- 고객·작가는 같은 `Member` 에 달린 프로필이라 **신원은 하나**고 역할은 "어떤 모자를 쓰고 신고했는지"에 불과하다. [[ADR-003 Party-Role 다중역할 모델링 + 역할 전환]]
- 같은 사람이 같은 대상을 중복 신고하는 것은 막아야 한다 → unique 제약이 필요하다.
- `ddl-auto: update` 라 CHECK 제약은 기존 테이블에 반영되지 않는다.

## 결정
- **대상은 대상별 nullable FK.** `targetType` 이 어느 대상인지 가리키고, 대상별 FK 컬럼(`artwork`, `chat_message`, …) 중 **정확히 하나만** 채운다.
- **"하나만 채워짐"은 정적 팩토리로 보장.** 생성자를 막고(`@Builder(access = PRIVATE)`) 대상별 정적 팩토리로만 생성 경로를 연다. CHECK 제약은 ddl-auto 한계로 못 쓴다.
- **신고자는 `Member` 하나로 합친다.** `Customer`/`Artist` 별 FK 를 두지 않는다.
- **`reporterRole` 은 스냅샷으로 저장.** 대상으로부터 유도 가능하지만(ARTWORK=고객, CHAT_MESSAGE=발신자의 반대) 어드민 조회를 단순하게 하려고 값으로 남긴다.
- **신고 방향 제약은 스키마가 아닌 애플리케이션에서.** 역할별 엔드포인트 분리 + "자기 것은 신고 불가" 검증(파사드).
- **중복 방지 unique 는 대상별로 따로 건다** — `(member_id, artwork_id)`, `(member_id, chat_message_id)` … MySQL 은 NULL 끼리 중복을 허용하므로 다른 타입 행끼리 간섭하지 않는다.
- **대상 추가 절차** — `ReportTargetType` 값 추가 → nullable `@ManyToOne` 필드 + 정적 팩토리 추가 → `(member_id, 새 FK)` unique 추가.

## 검토한 대안
| 대안 | 기각 이유 |
|---|---|
| 단일 `target_id` 다형성 컬럼 + `targetType` | **참조 무결성과 JOIN 을 둘 다 잃는다.** 대상이 삭제돼도 고아 행이 남고, 어드민 조회에서 매번 타입 분기 조인이 필요 |
| 대상 종류마다 테이블 분리(`ArtworkReport`, `ChatMessageReport`) | 상태·처리 흐름이 동일한데 테이블이 종류만큼 늘어난다. 어드민 목록이 UNION 지옥 |
| 신고자를 `Customer`/`Artist` 별 FK 로 | **unique 제약이 (신고자 종류 × 대상 종류) 로 곱해져 불어난다.** 게다가 같은 사람이 역할만 바꿔 같은 대상을 다시 신고할 수 있게 되는 구멍 |
| `reporterRole` 을 저장하지 않고 매번 유도 | 어드민 조회마다 대상 타입별 분기가 필요. 스냅샷 한 컬럼이 훨씬 싸다 |
| CHECK 제약으로 "하나만 채워짐" 강제 | `ddl-auto: update` 가 기존 테이블에 CHECK 를 넣지 않는다 → 있으나 마나 |

## 결과 (트레이드오프)
- ✅ 대상이 늘어도 테이블은 하나. 어드민 목록·상태 처리 로직이 공유된다.
- ✅ FK 를 살려서 대상 삭제 시 고아 행이 안 생기고, 조인으로 대상 정보를 바로 끌어온다.
- ✅ 신고자 축이 하나라 unique 가 대상 수만큼만 늘어난다(곱하지 않는다).
- ✅ 역할을 바꿔가며 같은 대상을 재신고하는 우회가 막힌다.
- ⚠️ **대상이 늘 때마다 nullable 컬럼이 하나씩 는다.** 종류가 아주 많아지면 sparse 테이블이 된다 — 5~6종을 넘어가면 재검토.
- ⚠️ "정확히 하나만 채워짐"이 **DB 가 아니라 코드로만 보장**된다. 정적 팩토리를 우회하면 깨진다.
- ⚠️ 회원 탈퇴 시 신고 행을 지운다 — 신고자 FK 가 `Member` 라 남겨두면 탈퇴자와 계속 이어지기 때문. 처리 이력이 필요해지면 신고자만 익명화하는 쪽으로 바꿔야 한다. [[ADR-010 회원 탈퇴·역할 해지 - soft delete·익명화·문의 자동취소]]

## 관련
- `domain/report/entity/{Report, ReportTargetType, ReportStatus}`
- `domain/report/service/{ReportCommandService, ReportQueryService}`, `ReportRepository`
- `domain/report/facade/ReportFacade`, `controller/CustomerReportController`

## 관련 노트
- [[ADR-003 Party-Role 다중역할 모델링 + 역할 전환]]
- [[ADR-010 회원 탈퇴·역할 해지 - soft delete·익명화·문의 자동취소]]
- [[회원 탈퇴 데이터 처리 규정]]
