package kr.co.dearbloom.domain.inquiry.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.dearbloom.domain.inquiry.entity.inquiry.Inquiry;

import java.time.LocalDate;
import java.time.LocalTime;

/** 스마트 문의 전송 결과. 전송 완료 화면에 그대로 보여줄 수 있게 문의 내용(스냅샷)을 함께 반환한다. */
@Schema(description = "스마트 문의 전송 결과")
public record InquiryCreateResponse(
        @Schema(description = "생성된 문의 ID", example = "1")
        Long inquiryId,
        @Schema(description = "작가명", example = "블루밍데이즈 스냅")
        String artistNickname,
        @Schema(description = "작품명", example = "야외 1인 졸업스냅")
        String artworkName,
        @Schema(description = "패키지명", example = "패키지 A")
        String packageName,
        @Schema(description = "촬영 날짜", example = "2026-06-11")
        LocalDate shootDate,
        @Schema(description = "촬영 시작 시각", example = "10:00")
        LocalTime startTime,
        @Schema(description = "촬영 소요시간(분)", example = "60")
        Integer durationMinutes,
        @Schema(description = "촬영 인원", example = "1")
        Integer headCount,
        @Schema(description = "학교명", example = "홍익대학교 서울캠퍼스")
        String schoolName,
        @Schema(description = "요청 사항", example = "자연스러운 보정 스타일을 선호해요.")
        String requestNote
) {
    public static InquiryCreateResponse from(Inquiry inquiry) {
        return new InquiryCreateResponse(
                inquiry.getInquiryId(),
                inquiry.getArtistNicknameSnapshot(),
                inquiry.getArtworkNameSnapshot(),
                inquiry.getPackageNameSnapshot(),
                inquiry.getShootDate(),
                inquiry.getStartTime(),
                inquiry.getDurationMinutesSnapshot(),
                inquiry.getHeadCount(),
                inquiry.getSchoolName(),
                inquiry.getRequestNote());
    }
}
