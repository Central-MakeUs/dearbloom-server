package kr.co.dearbloom.domain.board.dto.board.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.dearbloom.domain.board.entity.board.SharedBoard;

/**
 * 초대 링크 진입 화면용 보드 미리보기. 비로그인도 조회할 수 있으므로
 * <b>보드 내부 정보(작품·댓글·멤버 목록)는 담지 않는다.</b>
 */
@Schema(description = "공동보드 초대 미리보기")
public record SharedBoardInviteResponse(
        @Schema(description = "공동보드 ID (입장 후 이동용)", example = "1")
        Long sharedBoardId,

        @Schema(description = "보드 이름", example = "우정스냅 보드")
        String boardName,

        @Schema(description = "방장 이름", example = "김디어")
        String ownerName,

        @Schema(description = "현재 참여 인원(방장 포함)", example = "3")
        long memberCount,

        @Schema(description = "이 링크를 연 사람이 이미 참여 중인지. 비로그인이면 false",
                example = "false")
        boolean alreadyJoined
) {
    public static SharedBoardInviteResponse of(SharedBoard sharedBoard, long memberCount, boolean alreadyJoined) {
        return new SharedBoardInviteResponse(
                sharedBoard.getSharedBoardId(),
                sharedBoard.getBoardName(),
                sharedBoard.getOwner().getName(),
                memberCount,
                alreadyJoined);
    }
}
