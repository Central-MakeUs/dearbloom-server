package kr.co.dearbloom.domain.board.dto.artwork.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record SharedArtworkSummaryResponse(
        @Schema(description = "공유 작품 ID", example = "1")
        Long sharedArtworkId,

        @Schema(description = "작품명", example = "야외 개인 졸업스냅")
        String title,

        @Schema(description = "패키지 중 최저가(원). 패키지가 없으면 null.", example = "200000")
        Integer lowestPrice,

        @Schema(description = "최소 촬영 인원(1~6)", example = "2")
        Integer minHeadCount,

        @Schema(description = "최대 촬영 인원(1~6). null 이면 minHeadCount인 이상(제한 없음).", example = "3")
        Integer maxHeadCount,

        @Schema(description = "작가 닉네임", example = "블루밍데이즈 스냅")
        String artistNickname,

        @Schema(description = "작가 활동 지역 목록", example = "[\"SEOUL\", \"GYEONGGI_NORTH\"]")
        List<String> artistRegionList,

        @Schema(description = "대표 이미지 CDN URL(sortOrder 가 가장 앞선 사진). 사진이 없으면 null.",
                example = "https://dev-cdn.dearbloom.co.kr/artwork/uuid.webp")
        String thumbnailUrl,

        @Schema(description = "내가 공유작품 좋아요 등록했는지 여부",
                example = "false")
        Boolean isLiked
) {
}
