package kr.co.dearbloom.domain.customer.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.concurrent.ThreadLocalRandom;

@Getter
@AllArgsConstructor
public enum CustomerProfileImage {
    GREEN,
    GREY,
    BROWN,
    BLUE;

    private static final CustomerProfileImage[] VALUES = values();

    /** 온보딩 시 자동 배정용. 고객이 고르는 값이 아니라 서버가 균등하게 뿌린다. */
    public static CustomerProfileImage random() {
        return VALUES[ThreadLocalRandom.current().nextInt(VALUES.length)];
    }
}
