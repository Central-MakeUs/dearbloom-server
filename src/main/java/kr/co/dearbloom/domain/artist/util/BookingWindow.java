package kr.co.dearbloom.domain.artist.util;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * 예약 오픈 기간 정책. 예약 날짜는 <b>오늘부터 3개월</b>까지만 열린다.
 * 창 밖(3개월 초과)·과거 날짜는 규칙과 무관하게 가용성 0(예약 불가)으로 본다.
 */
public final class BookingWindow {
    public static final int OPEN_MONTHS = 3;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private BookingWindow() {
    }

    /** 예약 가능 시작일(오늘). */
    public static LocalDate firstOpenDate() {
        return LocalDate.now(KST);
    }

    /** 예약 가능 마지막일(오늘 + 3개월). */
    public static LocalDate lastOpenDate() {
        return LocalDate.now(KST).plusMonths(OPEN_MONTHS);
    }

    /** 해당 날짜가 예약 오픈 창 안(오늘 ≤ date ≤ 오늘+3개월)인지. */
    public static boolean isOpen(LocalDate date) {
        LocalDate today = LocalDate.now(KST);
        return !date.isBefore(today) && !date.isAfter(today.plusMonths(OPEN_MONTHS));
    }
}
