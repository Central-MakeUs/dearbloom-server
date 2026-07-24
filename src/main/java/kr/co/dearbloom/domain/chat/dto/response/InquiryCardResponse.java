package kr.co.dearbloom.domain.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.dearbloom.domain.inquiry.entity.Inquiry;

import java.time.LocalDate;
import java.time.LocalTime;

/** 문의 카드(message_type=INQUIRY) 렌더용. 값은 문의 스냅샷에서 조립하며 '작품상세 보기'는 artworkId 로 이동. */
@Schema(description = "문의 카드")
public record InquiryCardResponse(
        @Schema(description = "문의 ID", example = "1")
        Long inquiryId,

        @Schema(description = "작품 ID (작품상세 보기 이동용)", example = "10")
        Long artworkId,

        @Schema(description = "작품명", example = "야외 1인 졸업스냅")
        String artworkName,

        @Schema(description = "패키지명", example = "패키지 A")
        String packageName,

        @Schema(description = "작가명", example = "블루밍데이즈 스냅")
        String artistNickname,

        @Schema(description = "촬영 날짜", example = "2026-06-11")
        LocalDate shootDate,

        @Schema(description = "촬영 시작 시각", example = "10:00")
        LocalTime startTime,

        @Schema(description = "촬영 인원", example = "1")
        Integer headCount,

        @Schema(description = "학교명", example = "홍익대학교 서울캠퍼스")
        String schoolName,

        @Schema(description = "요청 사항", example = "자연스러운 보정 스타일을 선호해요.")
        String requestNote
) {
    public static InquiryCardResponse from(Inquiry inquiry) {
        return new InquiryCardResponse(
                inquiry.getInquiryId(),
                inquiry.getArtworkPackage().getArtwork().getArtworkId(),
                inquiry.getArtworkNameSnapshot(),
                inquiry.getPackageNameSnapshot(),
                inquiry.getArtistNicknameSnapshot(),
                inquiry.getShootDate(),
                inquiry.getStartTime(),
                inquiry.getHeadCount(),
                inquiry.getSchoolName(),
                inquiry.getRequestNote());
    }
}
