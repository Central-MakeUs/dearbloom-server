package kr.co.dearbloom.domain.artist.facade;

import kr.co.dearbloom.domain.artist.dto.schedule.request.DateBlockRequest;
import kr.co.dearbloom.domain.artist.dto.schedule.request.RecurringBlockRequest;
import kr.co.dearbloom.domain.artist.dto.schedule.request.WeeklyAvailabilityUpdateRequest;
import kr.co.dearbloom.domain.artist.dto.schedule.response.DayAvailabilityResponse;
import kr.co.dearbloom.domain.artist.dto.schedule.response.ScheduleRuleResponse;
import kr.co.dearbloom.domain.artist.dto.schedule.response.ScheduleSummaryResponse;
import kr.co.dearbloom.domain.artist.entity.artist.Artist;
import kr.co.dearbloom.domain.artist.entity.schedule.ScheduleRuleType;
import kr.co.dearbloom.domain.artist.service.schedule.ScheduleAvailabilityService;
import kr.co.dearbloom.domain.artist.service.schedule.ScheduleCommandService;
import kr.co.dearbloom.domain.artist.service.schedule.ScheduleQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ScheduleFacade {
    private final ScheduleCommandService scheduleCommandService;
    private final ScheduleQueryService scheduleQueryService;
    private final ScheduleAvailabilityService scheduleAvailabilityService;

    public List<DayAvailabilityResponse> getMyCalendar(Artist artist, LocalDate from, LocalDate to) {
        return scheduleAvailabilityService.getCalendar(artist, from, to);
    }

    // 지금 등록된 기본 촬영 가능 목록(PUT /weekly 편집 폼 prefill용).
    public List<ScheduleRuleResponse> getWeeklyAvailability(Artist artist) {
        return scheduleQueryService.getWeeklyAvailability(artist).stream()
                .map(ScheduleRuleResponse::from)
                .toList();
    }

    // 등록된 반복 예약 불가 목록(삭제 대상 id 확인용).
    public List<ScheduleRuleResponse> getRecurringBlocks(Artist artist) {
        return scheduleQueryService.getRecurringBlocks(artist).stream()
                .map(ScheduleRuleResponse::from)
                .toList();
    }

    // 등록된 개인 예약 불가 목록(삭제 대상 id 확인용).
    public List<ScheduleRuleResponse> getDateBlocks(Artist artist, LocalDate from, LocalDate to) {
        return scheduleQueryService.getDateBlocks(artist, from, to).stream()
                .map(ScheduleRuleResponse::from)
                .toList();
    }

    // 세 종류 규칙을 한 번에 묶어 반환(설정 화면 요약용). date-blocks 만 from~to 필터가 적용된다.
    public ScheduleSummaryResponse getScheduleSummary(Artist artist, LocalDate from, LocalDate to) {
        return new ScheduleSummaryResponse(
                getWeeklyAvailability(artist),
                getRecurringBlocks(artist),
                getDateBlocks(artist, from, to));
    }

    @Transactional
    public void replaceWeeklyAvailability(Artist artist, WeeklyAvailabilityUpdateRequest request) {
        scheduleCommandService.replaceWeeklyAvailable(artist, request.getAvailabilityList());
    }

    @Transactional
    public ScheduleRuleResponse addRecurringBlock(Artist artist, RecurringBlockRequest request) {
        return ScheduleRuleResponse.from(scheduleCommandService.addRecurringBlock(
                artist, request.getDayOfWeek(), request.getStartTime(), request.getEndTime()));
    }

    @Transactional
    public void deleteRecurringBlock(Artist artist, Long scheduleRuleId) {
        scheduleCommandService.deleteRule(artist, scheduleRuleId, ScheduleRuleType.WEEKLY_BLOCK);
    }

    @Transactional
    public ScheduleRuleResponse addDateBlock(Artist artist, DateBlockRequest request) {
        return ScheduleRuleResponse.from(scheduleCommandService.addDateBlock(
                artist, request.getDate(), request.getStartTime(), request.getEndTime()));
    }

    @Transactional
    public void deleteDateBlock(Artist artist, Long scheduleRuleId) {
        scheduleCommandService.deleteRule(artist, scheduleRuleId, ScheduleRuleType.DATE_BLOCK);
    }
}
