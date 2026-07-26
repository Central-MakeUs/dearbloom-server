package kr.co.dearbloom.domain.inquiry.dto.response.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.dearbloom.domain.inquiry.entity.Inquiry;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

/** 고객 문의 상세. 스냅샷 기반이라 작가/작품이 수정·삭제돼도 문의 시점 값이 유지된다. */
@Schema(description = "고객 문의 상세 조회 응답")
public record CustomerInquiryDetailResponse(
        @Schema(description = "문의 ID", example = "1")
        Long inquiryId,
        @Schema(description = "작가명", example = "블루밍데이즈 스냅")
        String artistNickname,
        @Schema(description = "작품명", example = "야외 1인 졸업스냅")
        String artworkName,
        @Schema(description = "패키지명", example = "패키지 A")
        String packageName,
        @Schema(description = "가격", example = "200000")
        Integer price,
        @Schema(description = "촬영 인원", example = "1")
        Integer headCount,
        @Schema(description = "작품 이미지(1장) URL")
        String artworkImageUrl,
        @Schema(description = "촬영 날짜", example = "2026-06-11")
        LocalDate shootDate,
        @Schema(description = "요일", example = "THURSDAY")
        DayOfWeek dayOfWeek,
        @Schema(description = "시작 시각", example = "10:00")
        LocalTime startTime,
        @Schema(description = "종료 시각", example = "11:00")
        LocalTime endTime,
        @Schema(description = "촬영 소요시간(분)", example = "60")
        Integer durationMinutes,
        @Schema(description = "학교명", example = "홍익대학교 서울캠퍼스")
        String schoolName,
        @Schema(description = "요청 사항", example = "자연스러운 보정 스타일을 선호해요.")
        String requestNote
) {
    public static CustomerInquiryDetailResponse of(Inquiry inquiry, String artworkImageUrl) {
        return new CustomerInquiryDetailResponse(
                inquiry.getInquiryId(),
                inquiry.getArtistNicknameSnapshot(),
                inquiry.getArtworkNameSnapshot(),
                inquiry.getPackageNameSnapshot(),
                inquiry.getPriceSnapshot(),
                inquiry.getHeadCount(),
                artworkImageUrl,
                inquiry.getShootDate(),
                inquiry.getShootDate().getDayOfWeek(),
                inquiry.getStartTime(),
                inquiry.getStartTime().plusMinutes(inquiry.getDurationMinutesSnapshot()),
                inquiry.getDurationMinutesSnapshot(),
                inquiry.getSchoolName(),
                inquiry.getRequestNote());
    }
}
