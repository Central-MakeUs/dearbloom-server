package kr.co.dearbloom.domain.inquiry.dto.response.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.dearbloom.domain.inquiry.entity.inquiry.Inquiry;
import kr.co.dearbloom.domain.inquiry.entity.inquiry.InquiryStatus;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

/** 고객 문의 리스트 항목. 최근 수정순으로 반환된다. */
@Schema(description = "고객 문의 리스트 항목")
public record CustomerInquiryListItemResponse(
        @Schema(description = "문의 ID", example = "1")
        Long inquiryId,
        @Schema(description = "문의 상태", example = "IN_PROGRESS")
        InquiryStatus status,
        @Schema(description = "상태 한국어 표기", example = "문의 진행중")
        String statusLabel,
        @Schema(description = "작가명", example = "블루밍데이즈 스냅")
        String artistNickname,
        @Schema(description = "작품명", example = "야외 1인 졸업스냅")
        String artworkName,
        @Schema(description = "작품 이미지(1장) URL")
        String artworkImageUrl,
        @Schema(description = "촬영 날짜", example = "2026-06-11")
        LocalDate shootDate,
        @Schema(description = "요일", example = "THURSDAY")
        DayOfWeek dayOfWeek,
        @Schema(description = "시작 시각", example = "10:00")
        LocalTime startTime
) {
    public static CustomerInquiryListItemResponse of(Inquiry inquiry, String artworkImageUrl) {
        return new CustomerInquiryListItemResponse(
                inquiry.getInquiryId(),
                inquiry.getStatus(),
                inquiry.getStatus().getLabel(),
                inquiry.getArtistNicknameSnapshot(),
                inquiry.getArtworkNameSnapshot(),
                artworkImageUrl,
                inquiry.getShootDate(),
                inquiry.getShootDate().getDayOfWeek(),
                inquiry.getStartTime());
    }
}
