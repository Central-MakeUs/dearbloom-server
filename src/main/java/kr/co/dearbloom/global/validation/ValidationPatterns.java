package kr.co.dearbloom.global.validation;

import java.util.regex.Pattern;

public class ValidationPatterns {
    public static final Pattern NICKNAME =
            Pattern.compile("^[가-힣a-zA-Z0-9_]{2,12}$");

    // 고객 실명. 2~12자의 한글 또는 영문 허용(공백·숫자 불가).
    // 상한이 12자인 건 온보딩에서 소셜 이름을 12자로 잘라 채우기 때문 — 그보다 짧으면 자동 생성된
    // 이름이 프로필 수정에서 반려된다(MemberFacade.PROFILE_NAME_MAX_LENGTH 와 맞춰야 함).
    public static final Pattern REAL_NAME =
            Pattern.compile("^[가-힣a-zA-Z]{2,12}$");

    private ValidationPatterns() {}
}
