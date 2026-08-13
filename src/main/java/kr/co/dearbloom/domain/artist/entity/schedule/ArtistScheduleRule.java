package kr.co.dearbloom.domain.artist.entity.schedule;

import jakarta.persistence.*;
import kr.co.dearbloom.domain.artist.entity.artist.Artist;
import kr.co.dearbloom.global.entity.BaseTime;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 작가 일정 규칙(sparse). 매 30분 셀을 저장하지 않고 규칙만 저장해 조회 시 합성한다.
 * - WEEKLY_AVAILABLE / WEEKLY_BLOCK : dayOfWeek 사용, blockDate 는 null
 * - DATE_BLOCK : blockDate 사용, dayOfWeek 는 null
 * 시간은 09:00~21:00, 30분 경계. (검증은 SlotGrid)
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
@Table(indexes = {
        // 작품 목록의 날짜 필터가 (작가, WEEKLY_AVAILABLE, 요일) 로 EXISTS 를 돌린다 — 세 컬럼이 다 인덱스에 있어야 커버된다.
        // 앞 두 컬럼이 prefix 라 기존 가용성 조회(artist_id + rule_type)도 이 인덱스로 함께 처리된다.
        @Index(name = "idx_schedule_artist_type_dow", columnList = "artist_id, rule_type, day_of_week"),
        @Index(name = "idx_schedule_artist_date", columnList = "artist_id, block_date")
})
public class ArtistScheduleRule extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scheduleRuleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id", nullable = false)
    private Artist artist;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false)
    private ScheduleRuleType ruleType;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week")
    private DayOfWeek dayOfWeek;

    @Column(name = "block_date")
    private LocalDate blockDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;
}
