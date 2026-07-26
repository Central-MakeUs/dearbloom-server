package kr.co.dearbloom.domain.artist.dto.schedule.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 기본 촬영 가능 일정 전체 교체. 보낸 목록으로 WEEKLY_AVAILABLE 전체를 덮어쓴다.
 * 빈 목록을 보내면 기본 가능 시간을 모두 비운다(→ 예약 불가).
 */
@Getter
@NoArgsConstructor
public class WeeklyAvailabilityUpdateRequest {
    @NotNull
    @Valid
    @Schema(description = "요일별 촬영 가능 시간대 리스트. 한 요일에 여러 구간을 넣을 수 있다.")
    private List<WeeklyRuleRequest> availabilityList;
}
