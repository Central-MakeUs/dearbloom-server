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
        int sharedArtworkCount,

        @Schema(description = "내가 안 읽은 댓글 수(내가 쓴 댓글 제외). 댓글 아이콘의 안읽음 뱃지에 씁니다. "
                + "0 이면 뱃지를 숨기세요. 이후 뱃지만 갱신할 땐 GET /{sharedBoardId}/comments/unread-count 를 쓰세요.",
                example = "1")
        long unreadCommentCount
) {
}
