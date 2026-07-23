package kr.co.dearbloom.domain.inquiry.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.dearbloom.domain.inquiry.entity.inquiry.InquiryHistory;
import kr.co.dearbloom.domain.inquiry.entity.inquiry.InquiryStatus;
import kr.co.dearbloom.domain.member.entity.MemberRole;

import java.time.LocalDateTime;

/** 문의 상태 변경 이력 1건 (타임라인). */
@Schema(description = "문의 상태 변경 이력 항목")
public record InquiryHistoryResponse(
        @Schema(description = "변경 전 상태(최초 생성이면 null)", example = "IN_PROGRESS")
        InquiryStatus fromStatus,
        @Schema(description = "변경 후 상태", example = "RESERVED")
        InquiryStatus toStatus,
        @Schema(description = "변경 후 상태 한국어 표기", example = "예약 완료")
        String toStatusLabel,
        @Schema(description = "변경 주체 role", example = "ARTIST")
        MemberRole changedByRole,
        @Schema(description = "변경 주체 한국어 표기", example = "작가")
        String changedByRoleLabel,
        @Schema(description = "변경 시각", example = "2026-06-05T14:30:00")
        LocalDateTime changedAt
) {
    public static InquiryHistoryResponse of(InquiryHistory history) {
        return new InquiryHistoryResponse(
                history.getFromStatus(),
                history.getToStatus(),
                history.getToStatus().getLabel(),
                history.getChangedByRole(),
                history.getChangedByRole().getLabel(),
                history.getCreatedAt());
    }
}
