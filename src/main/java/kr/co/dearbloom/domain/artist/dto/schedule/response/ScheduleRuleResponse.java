package kr.co.dearbloom.domain.artist.dto.schedule.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.dearbloom.domain.artist.entity.schedule.ArtistScheduleRule;
import kr.co.dearbloom.domain.artist.entity.schedule.ScheduleRuleType;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

/** 규칙 1건. 반복(요일) 규칙은 dayOfWeek, 개인 불가는 blockDate 가 채워진다. */
@Schema(description = "일정 규칙 1건")
public record ScheduleRuleResponse(
        Long scheduleRuleId,
        @Schema(description = "규칙 종류. WEEKLY_AVAILABLE(기본 촬영 가능) / WEEKLY_BLOCK(반복 예약 불가) / DATE_BLOCK(개인 예약 불가).",
                example = "WEEKLY_BLOCK")
        ScheduleRuleType ruleType,
        @Schema(description = "반복 규칙(WEEKLY_AVAILABLE/WEEKLY_BLOCK)일 때만", example = "SUNDAY")
        DayOfWeek dayOfWeek,
        @Schema(description = "개인 예약 불가(DATE_BLOCK)일 때만", example = "2026-06-15")
        LocalDate blockDate,
        LocalTime startTime,
        LocalTime endTime
) {
    public static ScheduleRuleResponse from(ArtistScheduleRule rule) {
        return new ScheduleRuleResponse(
                rule.getScheduleRuleId(),
                rule.getRuleType(),
                rule.getDayOfWeek(),
                rule.getBlockDate(),
                rule.getStartTime(),
                rule.getEndTime());
    }
}
