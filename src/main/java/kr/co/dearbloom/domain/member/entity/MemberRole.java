package kr.co.dearbloom.domain.member.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MemberRole {
    CUSTOMER("고객"),
    ARTIST("작가");

    private final String label;
}
