package kr.co.dearbloom.domain.inquiry.dto.response.artist;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.dearbloom.domain.inquiry.entity.Inquiry;
import kr.co.dearbloom.domain.inquiry.entity.InquiryStatus;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

/** 작가 문의 리스트 항목. 최근 수정순으로 반환된다. */
@Schema(description = "작가 문의 리스트 항목")
public record ArtistInquiryListItemResponse(
        @Schema(description = "문의 ID", example = "1")
        Long inquiryId,
        @Schema(description = "문의 상태", example = "IN_PROGRESS")
        InquiryStatus status,
        @Schema(description = "상태 한국어 표기", example = "문의 진행중")
        String statusLabel,
        @Schema(description = "작품명", example = "야외 1인 졸업스냅")
        String artworkName,
        @Schema(description = "패키지명", example = "패키지 A")
        String packageName,
        @Schema(description = "촬영 인원", example = "1")
        Integer headCount,
        @Schema(description = "학교명", example = "홍익대학교 서울캠퍼스")
        String schoolName,
        @Schema(description = "촬영 날짜", example = "2026-06-11")
        LocalDate shootDate,
        @Schema(description = "요일", example = "THURSDAY")
        DayOfWeek dayOfWeek,
        @Schema(description = "시작 시각", example = "10:00")
        LocalTime startTime,
        @Schema(description = "촬영 소요시간(분)", example = "60")
        Integer durationMinutes
) {
    public static ArtistInquiryListItemResponse of(Inquiry inquiry) {
        return new ArtistInquiryListItemResponse(
                inquiry.getInquiryId(),
                inquiry.getStatus(),
                inquiry.getStatus().getLabel(),
                inquiry.getArtworkNameSnapshot(),
                inquiry.getPackageNameSnapshot(),
                inquiry.getHeadCount(),
                inquiry.getSchoolName(),
                inquiry.getShootDate(),
                inquiry.getShootDate().getDayOfWeek(),
                inquiry.getStartTime(),
                inquiry.getDurationMinutesSnapshot());
    }
}
