> **ADR-002 · Search · Accepted · 2026-06-29**
> 대학교 검색 자동완성을 어떤 저장소/알고리즘으로 구현할지 결정. → **Redis Sorted Set(ZSet) 사전식 범위 + 한글 초성 prefix 매칭.**

## 맥락
- 온보딩에서 고객이 대학교를 검색해 고르는데, 타이핑마다 자동완성이 필요.
- 데이터는 전국 대학 **수백 건(~400)** 수준으로 소량, 거의 불변.
- 한국어 특성상 **초성 검색**("ㅅㅇ" → 서울대")과 완성형 prefix 둘 다 지원해야 UX가 자연스럽다.

## 결정
- **Redis ZSet** 에 대학 목록을 인덱싱한다. (key `university:autocomplete`, 멤버 = `이름|캠퍼스|지역|주소|ID`, score 0 → **lexicographical 정렬**)
- 검색: 입력에서 **자음(초성) 나오기 전까지의 완성형 prefix** 로 `ZRANGEBYLEX` 1차 축소 → `HangulUtils.matchesPrefix` 로 초성 포함 2차 필터.
- 인덱스는 앱 기동 시 `ApplicationRunner`(`UniversityAutocompleteInitializer`) 로 DB에서 읽어 빌드.

## 검토한 대안
| 대안 | 기각 이유 |
|---|---|
| DB `LIKE '%q%'` | 초성 검색 불가, 매 타이핑마다 풀스캔성 쿼리 |
| MySQL Full-Text | 한국어/초성 토크나이징 부적합, 설정 부담 |
| Elasticsearch | 수백 건에 오버킬(운영·인프라 비용 과다) |
| 애플리케이션 인메모리 | 서버 다중화 시 각자 로딩, 공유 안 됨(Redis가 이미 있음) |

## 결과 (트레이드오프)
- ✅ 초성+완성형 prefix 를 빠르게. 소량 데이터라 `ZRANGEBYLEX` + 인메모리 필터로 충분.
- ✅ 이미 쓰는 Redis 재사용, 별도 검색엔진 불필요.
- ⚠️ 기동 시 인덱스 재빌드(데이터 소량이라 무시 가능). 대학 데이터 갱신 시 재빌드 필요.
- ⚠️ 초성 매칭은 **선두 완성형 prefix 로 범위를 좁힌 뒤** 필터하는 구조라, 첫 글자부터 자음이면 범위 축소가 안 돼 전체 조회 후 필터(데이터 소량이라 허용).

## 관련
- `domain/university/repository/UniversityAutocompleteRepository`, `global/util/HangulUtils`, `UniversityAutocompleteInitializer`
