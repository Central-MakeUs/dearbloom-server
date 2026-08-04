package kr.co.dearbloom.domain.board.dto.board.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.dearbloom.domain.board.entity.board.SharedMember;

/** 공동보드 입장 결과(생성된 공유멤버 + 입장한 보드). */
public record SharedBoardJoinResponse(
        @Schema(description = "공유멤버 ID", example = "12")
        Long sharedMemberId,

        @Schema(description = "공동보드 ID", example = "1")
        Long sharedBoardId,

        @Schema(description = "보드 이름", example = "우리 졸업스냅 모음")
        String boardName
) {
    public static SharedBoardJoinResponse from(SharedMember sharedMember) {
        return new SharedBoardJoinResponse(
                sharedMember.getSharedMemberId(),
                sharedMember.getSharedBoard().getSharedBoardId(),
                sharedMember.getSharedBoard().getBoardName());
    }
}
