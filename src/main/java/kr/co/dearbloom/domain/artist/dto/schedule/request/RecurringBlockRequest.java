package kr.co.dearbloom.domain.artist.dto.schedule.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

/** 반복 예약 불가(요일별 차단) 1건 추가. */
@Getter
@NoArgsConstructor
public class RecurringBlockRequest {
    @NotNull
    @Schema(description = "차단할 요일", example = "SUNDAY",
            allowableValues = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"})
    private DayOfWeek dayOfWeek;

    @NotNull
    @Schema(description = "시작 시각 (09:00~21:00, 30분 단위)", example = "12:00", type = "string")
    private LocalTime startTime;

    @NotNull
    @Schema(description = "종료 시각 (09:00~21:00, 30분 단위, 시작보다 뒤)", example = "13:00", type = "string")
    private LocalTime endTime;
}
