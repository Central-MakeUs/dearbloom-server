package kr.co.dearbloom.domain.board.dto.artwork.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.dearbloom.domain.board.entity.artwork.SharedArtwork;

/** 공유작품 한 건의 최소 정보(ID + 작품명). 공유작품 업데이트 응답에 사용. */
public record SharedArtworkItemResponse(
        @Schema(description = "공유작품 ID", example = "12")
        Long sharedArtworkId,

        @Schema(description = "작품명", example = "야외 개인 졸업스냅")
        String title
) {
    public static SharedArtworkItemResponse from(SharedArtwork sharedArtwork) {
        return new SharedArtworkItemResponse(
                sharedArtwork.getSharedArtworkId(),
                sharedArtwork.getArtwork().getArtworkName());
    }
}
