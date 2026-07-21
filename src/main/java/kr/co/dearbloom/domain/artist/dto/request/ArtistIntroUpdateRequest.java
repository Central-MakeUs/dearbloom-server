package kr.co.dearbloom.domain.artist.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ArtistIntroUpdateRequest {
    @Size(max = 255, message = "소개는 255자 이하여야 합니다")
    @Schema(description = "작가 소개. 빈 문자열을 보내면 소개를 비웁니다.",
            example = "졸업스냅 전문 작가입니다.")
    private String intro;
}
