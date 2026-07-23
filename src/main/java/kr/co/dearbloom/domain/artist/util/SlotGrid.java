package kr.co.dearbloom.domain.artist.util;

import kr.co.dearbloom.global.dto.response.exception.CustomException;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 고정 시간 그리드. 09:00~21:00, 30분 단위 = 하루 24칸(index 0..23).
 * 가용성은 24비트 마스크(int)로 표현한다. bit i = 1 → 셀 i([09:00+30*i, +30분)) 가용.
 */
public final class SlotGrid {
    public static final LocalTime OPEN = LocalTime.of(9, 0);
    public static final LocalTime CLOSE = LocalTime.of(21, 0);
    public static final int STEP_MINUTES = 30;
    public static final int SLOT_COUNT = 24; // (21-9)*60/30
    // 하위 SLOT_COUNT(24)비트를 전부 1로 채운 마스크. 1을 24칸 시프트해 25번째 비트만 세운 뒤 1을 빼면
    // 그 비트가 사라지며 아래 24비트가 전부 1로 뒤집히는 원리(0b1000...0 - 1 = 0b0111...1).
    public static final int FULL_MASK = (1 << SLOT_COUNT) - 1;
    private SlotGrid() {
    }

    /** LocalTime → 셀 인덱스. 시작셀은 0..23, 종료 경계는 24(=21:00)까지 허용. */
    public static int toIndex(LocalTime time) {
        return ((time.getHour() - OPEN.getHour()) * 60 + time.getMinute()) / STEP_MINUTES;
    }

    /** 셀 인덱스 → 해당 셀 시작 시각. */
    public static LocalTime toTime(int index) {
        return OPEN.plusMinutes((long) index * STEP_MINUTES);
    }

    /** [start, end) 구간이 덮는 셀들의 비트마스크. */
    public static int rangeMask(LocalTime start, LocalTime end) {
        int mask = 0;
        for (int i = toIndex(start); i < toIndex(end); i++) {
            mask |= (1 << i);
        }
        return mask;
    }

    /** 마스크에서 켜진 셀들의 시작 시각 목록(오름차순). */
    public static List<LocalTime> toTimes(int mask) {
        List<LocalTime> times = new ArrayList<>();
        for (int i = 0; i < SLOT_COUNT; i++) {
            if ((mask & (1 << i)) != 0) {
                times.add(toTime(i));
            }
        }
        return times;
    }

    /**
     * N칸 연속 예약 가능한 "시작셀" 마스크. bit i = 1 → 셀 [i, i+N) 이 모두 available.
     * 우측 시프트 AND 로 접으면 21:00 초과(끝을 벗어나는) 시작셀은 상위 비트가 0이라 자동 제외된다.
     */
    public static int startableMask(int availableMask, int requiredSlots) {
        int mask = availableMask;
        for (int k = 1; k < requiredSlots; k++) {
            mask &= (availableMask >>> k);
        }
        return mask & FULL_MASK;
    }

    /** 소요시간(분)을 필요한 30분 셀 수로 환산(올림). 예: 60→2, 90→3, 45→2. */
    public static int requiredSlots(int durationMinutes) {
        return (durationMinutes + STEP_MINUTES - 1) / STEP_MINUTES;
    }

    /** 시작 시각 → 시작셀 인덱스(0..23). 30분 경계·[09:00, 21:00) 범위가 아니면 예외. */
    public static int toStartIndex(LocalTime start) {
        if (!isBoundary(start) || !start.isBefore(CLOSE)) {
            throw new CustomException(ErrorCode.INVALID_SCHEDULE_TIME);
        }
        return toIndex(start);
    }

    /** 규칙/요청 시간 검증. 09:00~21:00, 30분 경계, start < end 아니면 예외. */
    public static void validateRange(LocalTime start, LocalTime end) {
        if (!isBoundary(start) || !isBoundary(end) || !end.isAfter(start)) {
            throw new CustomException(ErrorCode.INVALID_SCHEDULE_TIME);
        }
    }

    private static boolean isBoundary(LocalTime time) {
        return time != null
                && !time.isBefore(OPEN) && !time.isAfter(CLOSE)
                && time.getMinute() % STEP_MINUTES == 0
                && time.getSecond() == 0 && time.getNano() == 0;
    }
}
