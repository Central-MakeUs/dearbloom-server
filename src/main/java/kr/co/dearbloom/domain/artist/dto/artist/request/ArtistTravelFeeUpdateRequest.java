package kr.co.dearbloom.domain.artist.dto.artist.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ArtistTravelFeeUpdateRequest {
    @Size(max = 5000, message = "출장비 안내는 5000자 이하여야 합니다")
    @Schema(description = "출장비 안내(지역별 금액 등, 자유 형식). 빈 문자열을 보내면 비웁니다.",
            example = "서울 전지역 무료 / 경기 3만원~ / 그 외 지역 별도 문의")
    private String travelFee;
}
