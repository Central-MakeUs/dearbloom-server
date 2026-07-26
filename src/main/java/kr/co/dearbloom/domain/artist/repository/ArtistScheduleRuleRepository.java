package kr.co.dearbloom.domain.artist.repository;

import kr.co.dearbloom.domain.artist.entity.artist.Artist;
import kr.co.dearbloom.domain.artist.entity.schedule.ArtistScheduleRule;
import kr.co.dearbloom.domain.artist.entity.schedule.ScheduleRuleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ArtistScheduleRuleRepository extends JpaRepository<ArtistScheduleRule, Long> {
    // 요일 반복 규칙(기본 가능 + 반복 불가)을 한 번에 조회(가용성 계산용).
    List<ArtistScheduleRule> findByArtistAndRuleTypeIn(Artist artist, Collection<ScheduleRuleType> ruleTypes);

    // 요일 반복 규칙 단일 타입 조회(설정 화면 목록용). 요일 → 시작시각 순 정렬.
    List<ArtistScheduleRule> findByArtistAndRuleTypeOrderByDayOfWeekAscStartTimeAsc(
            Artist artist, ScheduleRuleType ruleType);

    // 특정 날짜의 개인 예약 불가.
    List<ArtistScheduleRule> findByArtistAndRuleTypeAndBlockDate(
            Artist artist, ScheduleRuleType ruleType, LocalDate blockDate);

    // 날짜 범위의 개인 예약 불가(캘린더 조회·설정 화면 목록 공용). 날짜 → 시작시각 순 정렬.
    List<ArtistScheduleRule> findByArtistAndRuleTypeAndBlockDateBetweenOrderByBlockDateAscStartTimeAsc(
            Artist artist, ScheduleRuleType ruleType, LocalDate from, LocalDate to);

    // 소유권 검증용 단건 조회.
    Optional<ArtistScheduleRule> findByScheduleRuleIdAndArtist(Long scheduleRuleId, Artist artist);

    // 기본 촬영 가능 전체 교체 시 기존 행 제거.
    void deleteByArtistAndRuleType(Artist artist, ScheduleRuleType ruleType);
}
