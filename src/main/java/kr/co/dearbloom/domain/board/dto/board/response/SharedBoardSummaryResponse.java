package kr.co.dearbloom.domain.board.dto.board.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** 내가 속한 공동보드 목록의 카드 한 장(미리보기 이미지 최대 4장 + 보드명 + 작품 수). */
public record SharedBoardSummaryResponse(
        @Schema(description = "공동보드 ID", example = "1")
        Long sharedBoardId,

        @Schema(description = "공동보드 이름", example = "우리 졸업스냅 모음")
        String sharedBoardName,

        @Schema(description = "보드에 담긴 공유작품 개수", example = "7")
        int sharedArtworkCount,

        @Schema(description = "미리보기 이미지 CDN URL 목록. 보드에 담긴 작품들의 첫 번째 사진이며 최대 4장"
                + "(작품이 4개 미만이면 담긴 작품 수만큼, 없으면 빈 배열)",
                example = "[\"https://dev-cdn.dearbloom.co.kr/artwork/uuid.webp\"]")
        List<String> thumbnailUrlList
) {
}
