package kr.co.dearbloom.domain.artist.entity.schedule;

/**
 * 작가 일정 규칙 3종. 가용성은 이 규칙들의 합성으로 계산한다.
 * available = WEEKLY_AVAILABLE − WEEKLY_BLOCK − DATE_BLOCK − (예약확정)
 */
public enum ScheduleRuleType {
    WEEKLY_AVAILABLE, // 기본 촬영 가능 (요일별 반복)
    WEEKLY_BLOCK,     // 반복 예약 불가 (요일별 반복)
    DATE_BLOCK        // 개인 예약 불가 (특정 날짜 1회성)
}
