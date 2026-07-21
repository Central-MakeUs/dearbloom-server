package kr.co.dearbloom.domain.artwork.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 작품 패키지 1개. 작품 등록 시 여러 개를 함께 등록한다. */
@Getter
@NoArgsConstructor
public class ArtworkPackageRequest {
    @NotBlank
    @Schema(description = "패키지명", example = "1인 60분 촬영")
    private String packageName;

    @NotNull
    @PositiveOrZero
    @Schema(description = "가격(원)", example = "200000")
    private Integer price;

    @PositiveOrZero
    @Schema(description = "촬영 시간(분). 미정이면 null.", example = "60")
    private Integer durationMinutes;

    @PositiveOrZero
    @Schema(description = "보정본 수. 미정이면 null.", example = "7")
    private Integer finalPhotoCount;

    @Schema(description = "추가 정보(변동 정보, 자유 텍스트). 없으면 null.",
            example = "빈티지 디카 추가 촬영 가능")
    private String extraInfo;
}
