package kr.co.dearbloom.domain.artist.dto.schedule.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** 특정 날짜의 합성된 가용 셀. availableTimes 는 예약 가능한 30분 셀들의 시작 시각(오름차순). */
@Schema(description = "날짜별 가용 슬롯")
public record DayAvailabilityResponse(
        @Schema(description = "날짜", example = "2026-06-11")
        LocalDate date,
        @Schema(description = "예약 가능한 30분 셀 시작 시각 리스트", example = "[\"10:00\",\"10:30\",\"11:00\"]")
        List<LocalTime> availableTimes
) {
}
