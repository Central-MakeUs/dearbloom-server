package kr.co.dearbloom.domain.board.dto.artwork.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.dearbloom.domain.artwork.dto.response.ArtworkSummaryResponse;
import kr.co.dearbloom.domain.board.dto.board.response.SharedMemberResponse;

public record SavedArtworkIsSharedResponse(
        @Schema(description = "작품 리스트 항목(카드)")
        ArtworkSummaryResponse artworkSummaryResponse,

        @Schema(description = "내가 이 보드에 담은 작품인지 여부. 체크 상태로 그대로 쓰면 됩니다.",
                example = "false")
        Boolean isShared,

        @Schema(description = "이 작품을 보드에 담은 참여자. <b>아무도 담지 않았으면 null</b> 이며 그때만 새로 담을 수 있습니다. "
                + "isShared=true 면 나 자신, false 인데 값이 있으면 다른 참여자가 담은 것이라 선택할 수 없습니다.",
                nullable = true)
        SharedMemberResponse sharedBy
) {
}
