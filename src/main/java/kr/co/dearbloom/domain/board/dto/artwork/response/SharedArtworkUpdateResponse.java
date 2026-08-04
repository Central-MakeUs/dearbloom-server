package kr.co.dearbloom.domain.board.dto.artwork.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.dearbloom.domain.board.entity.board.SharedBoard;

import java.util.List;

/** 공유작품 업데이트 결과(보드 정보 + 업데이트 후 내 공유작품 목록). */
public record SharedArtworkUpdateResponse(
        @Schema(description = "공동보드 ID", example = "1")
        Long sharedBoardId,

        @Schema(description = "보드 이름", example = "우리 졸업스냅 모음")
        String sharedBoardName,

        @Schema(description = "업데이트 후 내가 이 보드에 공유 중인 작품 목록")
        List<SharedArtworkItemResponse> sharedArtworkList
) {
    public static SharedArtworkUpdateResponse of(SharedBoard sharedBoard,
                                                 List<SharedArtworkItemResponse> sharedArtworkList) {
        return new SharedArtworkUpdateResponse(
                sharedBoard.getSharedBoardId(),
                sharedBoard.getBoardName(),
                sharedArtworkList);
    }
}
