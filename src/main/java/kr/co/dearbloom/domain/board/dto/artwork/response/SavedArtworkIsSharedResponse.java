package kr.co.dearbloom.domain.board.dto.artwork.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.dearbloom.domain.artwork.dto.response.ArtworkSummaryResponse;

public record SavedArtworkIsSharedResponse(
        @Schema(description = "작품 리스트 항목(카드)")
        ArtworkSummaryResponse artworkSummaryResponse,

        @Schema(description = "내 저장 작품이 공동보드의 공유작품으로 공유되어 었는지 여부",
                example = "false")
        Boolean isShared
) {
}
