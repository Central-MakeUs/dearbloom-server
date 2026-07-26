package kr.co.dearbloom.domain.artist.service.schedule;

import kr.co.dearbloom.domain.artist.entity.artist.Artist;
import kr.co.dearbloom.domain.artist.entity.schedule.ArtistScheduleRule;
import kr.co.dearbloom.domain.artist.entity.schedule.ScheduleRuleType;
import kr.co.dearbloom.domain.artist.repository.ArtistScheduleRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 작가가 등록해둔 일정 규칙 원본(raw)을 조회한다. 합성된 가용 셀은 {@link ScheduleAvailabilityService} 담당.
 * 설정 화면에서 "지금 등록된 값"을 보여주거나(수정 폼 prefill), 삭제 대상 id 목록을 보여줄 때 쓴다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleQueryService {
    private final ArtistScheduleRuleRepository ruleRepository;

    // 기본 촬영 가능(WEEKLY_AVAILABLE) 전체 목록.
    public List<ArtistScheduleRule> getWeeklyAvailability(Artist artist) {
        return ruleRepository.findByArtistAndRuleTypeOrderByDayOfWeekAscStartTimeAsc(
                artist, ScheduleRuleType.WEEKLY_AVAILABLE);
    }

    // 반복 예약 불가(WEEKLY_BLOCK) 전체 목록.
    public List<ArtistScheduleRule> getRecurringBlocks(Artist artist) {
        return ruleRepository.findByArtistAndRuleTypeOrderByDayOfWeekAscStartTimeAsc(
                artist, ScheduleRuleType.WEEKLY_BLOCK);
    }

    // 개인 예약 불가(DATE_BLOCK) 목록. from~to 기간으로 필터링.
    public List<ArtistScheduleRule> getDateBlocks(Artist artist, LocalDate from, LocalDate to) {
        return ruleRepository.findByArtistAndRuleTypeAndBlockDateBetweenOrderByBlockDateAscStartTimeAsc(
                artist, ScheduleRuleType.DATE_BLOCK, from, to);
    }
}
