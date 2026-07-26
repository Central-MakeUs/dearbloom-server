package kr.co.dearbloom.domain.artist.dto.schedule.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/** 개인 예약 불가(특정 날짜 차단) 1건 추가. */
@Getter
@NoArgsConstructor
public class DateBlockRequest {
    @NotNull
    @Schema(description = "차단할 날짜", example = "2026-06-15", type = "string")
    private LocalDate date;

    @NotNull
    @Schema(description = "시작 시각 (09:00~21:00, 30분 단위)", example = "09:00", type = "string")
    private LocalTime startTime;

    @NotNull
    @Schema(description = "종료 시각 (09:00~21:00, 30분 단위, 시작보다 뒤)", example = "21:00", type = "string")
    private LocalTime endTime;
}
