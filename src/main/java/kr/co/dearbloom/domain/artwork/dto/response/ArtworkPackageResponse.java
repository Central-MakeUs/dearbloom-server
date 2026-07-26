package kr.co.dearbloom.domain.artwork.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.dearbloom.domain.artwork.entity.ArtworkPackage;

/** 작품 패키지 응답. 작품 상세에서 패키지 목록으로 노출. */
public record ArtworkPackageResponse(
        @Schema(description = "패키지 ID", example = "1")
        Long artworkPackageId,

        @Schema(description = "패키지명", example = "1인 60분 촬영")
        String packageName,

        @Schema(description = "가격(원)", example = "200000")
        Integer price,

        @Schema(description = "촬영 시간(분). 미정이면 null.", example = "60")
        Integer durationMinutes,

        @Schema(description = "보정본 수. 미정이면 null.", example = "7")
        Integer finalPhotoCount,

        @Schema(description = "추가 정보(자유 텍스트). 없으면 null.", example = "빈티지 디카 추가 촬영 가능")
        String extraInfo
) {
    public static ArtworkPackageResponse from(ArtworkPackage artworkPackage) {
        return new ArtworkPackageResponse(
                artworkPackage.getArtworkPackageId(),
                artworkPackage.getPackageName(),
                artworkPackage.getPrice(),
                artworkPackage.getDurationMinutes(),
                artworkPackage.getFinalPhotoCount(),
                artworkPackage.getExtraInfo()
        );
    }
}
