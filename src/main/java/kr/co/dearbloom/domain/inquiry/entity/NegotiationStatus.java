package kr.co.dearbloom.domain.inquiry.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NegotiationStatus {
    PROPOSED("제안됨"),
    ACCEPTED("수락"),
    REJECTED("거절");

    private final String label;
}
