package kr.co.dearbloom.global.validation;

import java.util.regex.Pattern;

public class ValidationPatterns {
    public static final Pattern NICKNAME =
            Pattern.compile("^[가-힣a-zA-Z0-9_]{2,12}$");

    // 고객 실명. 2~5자의 한글 또는 영문 허용.
    public static final Pattern REAL_NAME =
            Pattern.compile("^[가-힣a-zA-Z]{2,5}$");

    private ValidationPatterns() {}
}
