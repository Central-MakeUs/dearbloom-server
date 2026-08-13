package kr.co.dearbloom.domain.artwork.dto;

import kr.co.dearbloom.domain.artwork.dto.type.ArtworkSortOrder;
import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.global.dto.response.exception.CustomException;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;

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

    /**
     * 이 정렬이 쓸 키가 다 들어 있는지 확인한다.
     * 커서는 클라이언트가 돌려보내는 값이라 잘리거나 손댈 수 있는데, 형식만 맞고 값이 빈 커서(예: "{}")를
     * 그대로 쿼리에 넘기면 조건을 만들다 NPE 로 500 이 난다. 그 전에 400 으로 끊는다.
     */
    public void validateFor(ArtworkSortOrder sort) {
        boolean hasSortKey = switch (sort) {
            case LATEST -> createdAt != null;
            case PRICE_LOW, PRICE_HIGH -> lowestPrice != null;
        };
        if (artworkId == null || !hasSortKey) {
            throw new CustomException(ErrorCode.INVALID_CURSOR);
        }
    }
}
