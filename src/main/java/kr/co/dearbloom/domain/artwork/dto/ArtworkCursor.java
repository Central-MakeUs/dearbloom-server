package kr.co.dearbloom.domain.artwork.dto;

import kr.co.dearbloom.domain.artwork.entity.Artwork;

import java.time.LocalDateTime;

/**
 * 작품 목록 커서 = 직전 페이지 마지막 행의 정렬 키.
 * 정렬에 따라 실제로 쓰이는 1차 키가 다르지만(최신순=createdAt, 가격순=lowestPrice)
 * 세 값을 모두 담아 둔다 — 커서가 작아서 아낄 이유가 없고, 정렬별 분기가 사라진다.
 * artworkId 는 1차 키가 같은 행들을 가르는 tie-breaker 라 어떤 정렬에서도 쓰인다.
 */
public record ArtworkCursor(
        LocalDateTime createdAt,
        Integer lowestPrice,
        Long artworkId
) {
    public static ArtworkCursor from(Artwork artwork) {
        return new ArtworkCursor(
                artwork.getCreatedAt(),
                artwork.getLowestPrice(),
                artwork.getArtworkId());
    }
}
