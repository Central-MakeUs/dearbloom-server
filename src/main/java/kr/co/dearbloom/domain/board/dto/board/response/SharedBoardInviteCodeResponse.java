package kr.co.dearbloom.domain.board.dto.board.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.dearbloom.domain.board.entity.board.SharedBoard;

/**
 * 공유하기 화면용 초대 코드. 참여 중인 멤버만 받을 수 있다.
 * 공유 링크는 이 코드로 프론트가 조립한다.
 */
@Schema(description = "공동보드 초대 코드")
public record SharedBoardInviteCodeResponse(
        @Schema(description = "공동보드 ID", example = "1")
        Long sharedBoardId,

        @Schema(description = "초대 코드. 공유 링크의 마지막 경로로 사용합니다", example = "K7QM2X")
        String inviteCode
) {
    public static SharedBoardInviteCodeResponse from(SharedBoard sharedBoard) {
        return new SharedBoardInviteCodeResponse(
                sharedBoard.getSharedBoardId(),
                sharedBoard.getInviteCode());
    }
}
