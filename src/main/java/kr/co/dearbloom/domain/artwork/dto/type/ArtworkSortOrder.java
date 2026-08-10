package kr.co.dearbloom.domain.artwork.dto.type;

/**
 * 작품 목록 정렬. 가격 정렬은 카드에 노출되는 가격(패키지 중 최저가) 기준이다.
 * 어떤 정렬이든 작품 ID 를 마지막 키로 덧붙여 동점 행의 순서를 고정한다 — 커서 페이지네이션이 성립하려면 필요하다.
 */
public enum ArtworkSortOrder {
    LATEST,     // 기본순 = 등록 최신순
    PRICE_LOW,  // 낮은 가격순
    PRICE_HIGH  // 높은 가격순
}
