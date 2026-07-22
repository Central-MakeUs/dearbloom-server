package kr.co.dearbloom.domain.artist.dto.schedule.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 세 종류 일정 규칙을 한 번에 묶은 요약 조회 응답. 설정 화면에서 세 섹션을 동시에 보여줄 때 사용한다.
 * 개별 목적(편집 폼 prefill, 삭제 대상 id 확인)은 기존 개별 조회 API(/weekly, /recurring-blocks, /date-blocks)를 계속 쓴다.
 */
@Schema(description = "작가 일정 규칙 요약 (기본 촬영 가능 + 반복 예약 불가 + 개인 예약 불가)")
public record ScheduleSummaryResponse(
        @Schema(description = "기본 촬영 가능 리스트") List<ScheduleRuleResponse> weeklyAvailabilityList,
        @Schema(description = "반복 예약 불가 리스트") List<ScheduleRuleResponse> recurringBlockList,
        @Schema(description = "개인 예약 불가 리스트 (조회 기간 내)") List<ScheduleRuleResponse> dateBlockList
) {
}
