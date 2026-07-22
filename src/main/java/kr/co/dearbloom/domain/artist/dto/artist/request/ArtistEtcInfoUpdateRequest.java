package kr.co.dearbloom.domain.artist.dto.artist.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ArtistEtcInfoUpdateRequest {
    @Size(max = 5000, message = "기타 안내는 5000자 이하여야 합니다")
    @Schema(description = "기타 안내(촬영 취소·환불 규정 등, 자유 형식). 빈 문자열을 보내면 비웁니다.",
            example = "촬영 취소 및 변경은 2주 전까지 전액 환불 가능합니다.\n(단, 우천으로 인한 날짜 변경은 당일에도 가능)")
    private String etcInfo;
}
