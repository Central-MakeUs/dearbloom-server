package kr.co.dearbloom.domain.inquiry.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.dearbloom.domain.inquiry.entity.Inquiry;
import kr.co.dearbloom.domain.inquiry.entity.InquiryStatus;

@Schema(description = "문의 상태 전이 결과")
public record InquiryStatusResponse(
        @Schema(description = "문의 ID", example = "1")
        Long inquiryId,
        @Schema(description = "전이 후 상태", example = "RESERVED")
        InquiryStatus status,
        @Schema(description = "상태 한국어 표기", example = "예약 완료")
        String statusLabel
) {
    public static InquiryStatusResponse of(Inquiry inquiry) {
        return new InquiryStatusResponse(
                inquiry.getInquiryId(), inquiry.getStatus(), inquiry.getStatus().getLabel());
    }
}
