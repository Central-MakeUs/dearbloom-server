package kr.co.dearbloom.domain.board.dto.board.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.dearbloom.domain.board.entity.board.SharedMember;

/** 공동보드 입장 결과(참여자 본인 + 입장한 보드). */
public record SharedBoardJoinResponse(
        @Schema(description = "참여자 고객 ID(입장한 본인)", example = "3")
        Long customerId,

        @Schema(description = "공동보드 ID", example = "1")
        Long sharedBoardId,

        @Schema(description = "보드 이름", example = "우리 졸업스냅 모음")
        String sharedBoardName
) {
    public static SharedBoardJoinResponse from(SharedMember sharedMember) {
        return new SharedBoardJoinResponse(
                sharedMember.getCustomer().getCustomerId(),
                sharedMember.getSharedBoard().getSharedBoardId(),
                sharedMember.getSharedBoard().getBoardName());
    }
}
