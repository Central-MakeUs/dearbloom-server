package kr.co.dearbloom.domain.reservation.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReservationStatus {
    PENDING("예약 대기"),
    CONFIRMED("예약 확정"),
    COMPLETED("촬영 완료"),
    CANCELED("취소");

    private final String label;
}
