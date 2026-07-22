package kr.co.dearbloom.domain.artist.service.schedule;

import kr.co.dearbloom.domain.artist.dto.schedule.request.WeeklyRuleRequest;
import kr.co.dearbloom.domain.artist.entity.artist.Artist;
import kr.co.dearbloom.domain.artist.entity.schedule.ArtistScheduleRule;
import kr.co.dearbloom.domain.artist.entity.schedule.ScheduleRuleType;
import kr.co.dearbloom.domain.artist.repository.ArtistScheduleRuleRepository;
import kr.co.dearbloom.domain.artist.util.SlotGrid;
import kr.co.dearbloom.global.dto.response.exception.CustomException;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** 작가 일정 규칙 생성/삭제. 시간 검증(SlotGrid) 후 저장한다. */
@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleCommandService {
    private final ArtistScheduleRuleRepository ruleRepository;

    /** 기본 촬영 가능(WEEKLY_AVAILABLE) 전체 교체. 기존 행 삭제 후 새로 저장. */
    public void replaceWeeklyAvailable(Artist artist, List<WeeklyRuleRequest> availabilityList) {
        availabilityList.forEach(r -> SlotGrid.validateRange(r.getStartTime(), r.getEndTime()));
        ruleRepository.deleteByArtistAndRuleType(artist, ScheduleRuleType.WEEKLY_AVAILABLE);
        List<ArtistScheduleRule> rows = availabilityList.stream()
                .map(r -> ArtistScheduleRule.builder()
                        .artist(artist)
                        .ruleType(ScheduleRuleType.WEEKLY_AVAILABLE)
                        .dayOfWeek(r.getDayOfWeek())
                        .startTime(r.getStartTime())
                        .endTime(r.getEndTime())
                        .build())
                .toList();
        ruleRepository.saveAll(rows);
    }

    /** 반복 예약 불가(WEEKLY_BLOCK) 1건 추가. */
    public ArtistScheduleRule addRecurringBlock(Artist artist, DayOfWeek dayOfWeek, LocalTime start, LocalTime end) {
        SlotGrid.validateRange(start, end);
        return ruleRepository.save(ArtistScheduleRule.builder()
                .artist(artist)
                .ruleType(ScheduleRuleType.WEEKLY_BLOCK)
                .dayOfWeek(dayOfWeek)
                .startTime(start)
                .endTime(end)
                .build());
    }

    /** 개인 예약 불가(DATE_BLOCK) 1건 추가. */
    public ArtistScheduleRule addDateBlock(Artist artist, LocalDate date, LocalTime start, LocalTime end) {
        SlotGrid.validateRange(start, end);
        return ruleRepository.save(ArtistScheduleRule.builder()
                .artist(artist)
                .ruleType(ScheduleRuleType.DATE_BLOCK)
                .blockDate(date)
                .startTime(start)
                .endTime(end)
                .build());
    }

    /** 규칙 삭제. 본인 소유 + 기대한 타입이 아니면 404. */
    public void deleteRule(Artist artist, Long scheduleRuleId, ScheduleRuleType expectedType) {
        ArtistScheduleRule rule = ruleRepository.findByScheduleRuleIdAndArtist(scheduleRuleId, artist)
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_RULE_NOT_FOUND));
        if (rule.getRuleType() != expectedType) {
            throw new CustomException(ErrorCode.SCHEDULE_RULE_NOT_FOUND);
        }
        ruleRepository.delete(rule);
    }
}
