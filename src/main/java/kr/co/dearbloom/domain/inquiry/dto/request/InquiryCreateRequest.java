package kr.co.dearbloom.domain.inquiry.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 스마트 문의 전송 요청.
 * 학교는 대학 목록에서 고르면 {@code universityId}, 목록에 없어 직접 입력하면 {@code schoolName} 을 보낸다(둘 중 하나 필수).
 */
@Getter
@NoArgsConstructor
public class InquiryCreateRequest {
    @NotNull
    @Schema(description = "문의할 작품 패키지 ID", example = "1")
    private Long artworkPackageId;

    @NotNull
    @Schema(description = "촬영 날짜", example = "2026-06-11")
    private LocalDate shootDate;

    @NotNull
    @Schema(description = "촬영 시작 시각 (09:00~21:00, 30분 단위)", example = "10:00")
    private LocalTime startTime;

    @NotNull
    @Min(1)
    @Schema(description = "촬영 인원 (작품의 min~max 범위 내)", example = "1")
    private Integer headCount;

    @Schema(description = "대학 목록에서 고른 학교 ID. 자유입력이면 비워두고 schoolName 을 보냄", example = "1")
    private Long universityId;

    @Schema(description = "직접 입력한 학교명. 목록에서 골랐으면 비워둠", example = "홍익대학교 서울캠퍼스")
    private String schoolName;

    @Schema(description = "요청 사항(선택)", example = "자연스러운 보정 스타일을 선호해요.")
    private String requestNote;
}
