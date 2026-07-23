package kr.co.dearbloom.domain.artwork.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 작품 기본 정보(작품명 / 설명) 부분 수정. 각 필드는 null 이면 변경하지 않는다(PATCH). */
@Getter
@NoArgsConstructor
public class ArtworkInfoUpdateRequest {
    @Schema(description = "작품명. 보내지 않거나 null 이면 변경하지 않습니다.", example = "봄날의 졸업사진")
    private String title;

    @Schema(description = "작품 설명. 보내지 않거나 null 이면 변경하지 않습니다. 빈 문자열이면 설명을 비웁니다.",
            example = "졸업을 앞둔 학생분들을 위한 야외 스냅입니다.")
    private String description;
}
