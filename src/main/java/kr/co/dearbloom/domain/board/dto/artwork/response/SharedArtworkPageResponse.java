package kr.co.dearbloom.domain.board.dto.artwork.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.dearbloom.domain.board.dto.board.response.SharedMemberResponse;

import java.util.List;

/** 공동보드 공유작품 페이지(참여자 + 공유작품 목록) 한 화면. */
public record SharedArtworkPageResponse(
        @Schema(description = "참여 인원", example = "3")
        int sharedMemberCount,

        @Schema(description = "공유 멤버 목록(입장 순). 방장도 포함됩니다.")
        List<SharedMemberResponse> sharedMemberList,

        @Schema(description = "공유작품 목록. 좋아요 많은 순, 같으면 먼저 담긴 순입니다.")
        List<SharedArtworkSummaryResponse> sharedArtworkList,

        @Schema(description = "공유작품 개수", example = "5")
        int sharedArtworkCount
) {
}
