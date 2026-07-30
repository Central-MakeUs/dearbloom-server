package kr.co.dearbloom.domain.report.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ArtworkReportCreateRequest {
    @NotNull
    @Schema(description = "신고할 작품 ID", example = "1")
    private Long artworkId;

    @NotBlank
    @Size(max = 1000)
    @Schema(description = "신고 사유 (자유 텍스트, 최대 1000자)", example = "작품 설명과 실제 촬영 내용이 다릅니다.")
    private String content;
}
