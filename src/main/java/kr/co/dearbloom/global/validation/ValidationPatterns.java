package kr.co.dearbloom.global.validation;

import java.util.regex.Pattern;

public class ValidationPatterns {
    public static final Pattern NICKNAME =
            Pattern.compile("^[가-힣a-zA-Z0-9_]{2,12}$");

    private ValidationPatterns() {}
}
