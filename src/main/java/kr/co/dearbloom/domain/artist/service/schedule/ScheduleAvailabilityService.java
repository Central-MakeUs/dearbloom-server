package kr.co.dearbloom.domain.artist.service.schedule;

import kr.co.dearbloom.domain.artist.dto.schedule.response.DayAvailabilityResponse;
import kr.co.dearbloom.domain.artist.entity.artist.Artist;
import kr.co.dearbloom.domain.artist.entity.schedule.ArtistScheduleRule;
import kr.co.dearbloom.domain.artist.entity.schedule.ScheduleRuleType;
import kr.co.dearbloom.domain.artist.repository.ArtistScheduleRuleRepository;
import kr.co.dearbloom.domain.artist.util.BookingWindow;
import kr.co.dearbloom.domain.artist.util.SlotGrid;
import kr.co.dearbloom.global.dto.response.exception.CustomException;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 규칙을 합성해 특정 날짜/기간의 가용 셀 마스크를 계산한다.
 * available = 기본가능(요일) − 반복불가(요일) − 개인불가(날짜) − 예약확정(날짜)
 * 작가 본인 캘린더·고객 문의 조회가 이 계산을 공유한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleAvailabilityService {
    private static final int MAX_RANGE_DAYS = 100; // 캘린더 조회 span 상한(예약 오픈 창 3개월 + 여유)

    private final ArtistScheduleRuleRepository ruleRepository;
    private final BookedSlotProvider bookedSlotProvider;

    /** 특정 날짜의 가용 셀 24비트 마스크. */
    public int availableMask(Artist artist, LocalDate date) {
        List<ArtistScheduleRule> weekly = ruleRepository.findByArtistAndRuleTypeIn(
                artist, List.of(ScheduleRuleType.WEEKLY_AVAILABLE, ScheduleRuleType.WEEKLY_BLOCK));
        List<ArtistScheduleRule> dateBlocks = ruleRepository.findByArtistAndRuleTypeAndBlockDate(
                artist, ScheduleRuleType.DATE_BLOCK, date);
        int bookedMask = bookedSlotProvider.bookedMask(artist.getArtistId(), date);
        return composeMask(weekly, dateBlocks, bookedMask, date);
    }

    /** 기간 [from, to] 의 날짜별 가용 슬롯. */
    public List<DayAvailabilityResponse> getCalendar(Artist artist, LocalDate from, LocalDate to) {
        if (from == null || to == null || to.isBefore(from)) {
            throw new CustomException(ErrorCode.PARAMETER_BAD_REQUEST);
        }
        if (from.plusDays(MAX_RANGE_DAYS).isBefore(to)) {
            throw new CustomException(ErrorCode.PARAMETER_BAD_REQUEST);
        }

        List<ArtistScheduleRule> weekly = ruleRepository.findByArtistAndRuleTypeIn(
                artist, List.of(ScheduleRuleType.WEEKLY_AVAILABLE, ScheduleRuleType.WEEKLY_BLOCK));
        Map<LocalDate, List<ArtistScheduleRule>> dateBlocksByDate = ruleRepository
                .findByArtistAndRuleTypeAndBlockDateBetweenOrderByBlockDateAscStartTimeAsc(
                        artist, ScheduleRuleType.DATE_BLOCK, from, to).stream()
                .collect(Collectors.groupingBy(ArtistScheduleRule::getBlockDate));
        // 예약 확정 마스크는 기간 전체를 한 번에 조회(날짜별 루프 안에서 재조회하지 않음).
        Map<LocalDate, Integer> bookedByDate = bookedSlotProvider.bookedMasks(artist.getArtistId(), from, to);

        List<DayAvailabilityResponse> result = new ArrayList<>();
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            int mask = composeMask(
                    weekly, dateBlocksByDate.getOrDefault(date, List.of()),
                    bookedByDate.getOrDefault(date, 0), date);
            result.add(new DayAvailabilityResponse(date, SlotGrid.toTimes(mask)));
        }
        return result;
    }

    // 요일 규칙 + 그 날짜의 개인 불가 + 예약 확정(bookedMask)을 합성.
    private int composeMask(List<ArtistScheduleRule> weekly, List<ArtistScheduleRule> dateBlocks,
                            int bookedMask, LocalDate date) {
        // 예약 오픈 창(오늘~3개월) 밖·과거 날짜는 규칙과 무관하게 예약 불가.
        if (!BookingWindow.isOpen(date)) {
            return 0;
        }
        DayOfWeek dow = date.getDayOfWeek();
        int base = 0;
        int block = 0;
        for (ArtistScheduleRule r : weekly) {
            if (r.getDayOfWeek() != dow) {
                continue;
            }
            int mask = SlotGrid.rangeMask(r.getStartTime(), r.getEndTime());
            if (r.getRuleType() == ScheduleRuleType.WEEKLY_AVAILABLE) {
                base |= mask;
            } else {
                block |= mask;
            }
        }
        for (ArtistScheduleRule r : dateBlocks) {
            block |= SlotGrid.rangeMask(r.getStartTime(), r.getEndTime());
        }
        return base & ~block & ~bookedMask & SlotGrid.FULL_MASK;
    }
}
