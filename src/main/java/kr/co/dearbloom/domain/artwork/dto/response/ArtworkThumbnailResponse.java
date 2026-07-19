package kr.co.dearbloom.domain.artwork.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/** 작품 요약(작품 ID + 대표 이미지 1장). '이 작가의 다른 작품' 목록 등에 사용. */
public record ArtworkThumbnailResponse(
        @Schema(description = "작품 ID", example = "2")
        Long artworkId,

        @Schema(description = "대표 이미지 CDN URL(sortOrder 가 가장 앞선 사진)",
                example = "https://cdn.dearbloom.co.kr/artwork/uuid.webp")
        String imageUrl
) {
}
