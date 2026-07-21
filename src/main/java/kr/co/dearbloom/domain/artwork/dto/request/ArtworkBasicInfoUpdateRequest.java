package kr.co.dearbloom.domain.artwork.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ArtworkBasicInfoUpdateRequest {
    @Schema(description = "작품 제목. 보내지 않거나 null 이면 변경하지 않습니다.", example = "봄날의 졸업사진")
    private String title;

    @PositiveOrZero
    @Schema(description = "기본 가격(원). 보내지 않거나 null 이면 변경하지 않습니다.", example = "300000")
    private Integer price;
}
