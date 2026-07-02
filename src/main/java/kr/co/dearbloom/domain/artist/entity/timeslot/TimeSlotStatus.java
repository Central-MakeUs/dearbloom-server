package kr.co.dearbloom.domain.artist.entity.timeslot;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TimeSlotStatus {
    AVAILABLE("예약 가능"),
    RESERVED("예약 확정"),
    CLOSED("닫힘");

    private final String label;
}
