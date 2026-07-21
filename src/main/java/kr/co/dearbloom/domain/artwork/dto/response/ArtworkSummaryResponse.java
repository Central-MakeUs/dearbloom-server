package kr.co.dearbloom.domain.artwork.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** 작품 리스트 항목(카드). 목록/저장목록 등 리스트 조회에서 사용. */
public record ArtworkSummaryResponse(
        @Schema(description = "작품 ID", example = "1")
        Long artworkId,

        @Schema(description = "작품명", example = "야외 개인 졸업스냅")
        String title,

        @Schema(description = "기본 가격(원)", example = "200000")
        Integer price,

        @Schema(description = "최소 촬영 인원(1~6)", example = "2")
        Integer minHeadCount,

        @Schema(description = "최대 촬영 인원(1~6). null 이면 minHeadCount인 이상(제한 없음).", example = "3")
        Integer maxHeadCount,

        @Schema(description = "작가 닉네임", example = "블루밍데이즈 스냅")
        String artistNickname,

        @Schema(description = "작가 활동 지역 목록", example = "[\"SEOUL\", \"GYEONGGI\"]")
        List<String> artistRegionList,

        @Schema(description = "대표 이미지 CDN URL(sortOrder 가 가장 앞선 사진). 사진이 없으면 null.",
                example = "https://cdn.dearbloom.co.kr/artwork/uuid.webp")
        String thumbnailUrl,

        @Schema(description = "내가 저장한 작품인지 여부. 고객 조회 시에만 값이 있고, 비로그인은 null.",
                example = "false")
        Boolean isSaved
) {
}
