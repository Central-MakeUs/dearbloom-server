package kr.co.dearbloom.domain.board.dto.board.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.dearbloom.domain.board.entity.board.SharedBoard;

/** 공동보드 엔티티 대응 응답(보드 ID / 방장 ID / 보드 이름). 생성·수정·삭제 응답에 공통 사용. */
public record SharedBoardResponse(
        @Schema(description = "공동보드 ID", example = "1")
        Long sharedBoardId,

        @Schema(description = "방장(보드 생성자) 고객 ID", example = "3")
        Long ownerId,

        @Schema(description = "보드 이름", example = "우리 졸업스냅 모음")
        String sharedBoardName
) {
    public static SharedBoardResponse from(SharedBoard sharedBoard) {
        return new SharedBoardResponse(
                sharedBoard.getSharedBoardId(),
                sharedBoard.getOwner().getCustomerId(),
                sharedBoard.getBoardName());
    }
}
