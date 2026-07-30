package kr.co.dearbloom.domain.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/** 내가 이 작품을 신고했는지 여부. */
@Schema(description = "작품 신고 여부")
public record ArtworkReportedResponse(
        @Schema(description = "작품 ID", example = "1")
        Long artworkId,

        @Schema(description = "내가 이 작품을 신고했는지 여부", example = "true")
        boolean reported
) {
    public static ArtworkReportedResponse of(Long artworkId, boolean reported) {
        return new ArtworkReportedResponse(artworkId, reported);
    }
}
