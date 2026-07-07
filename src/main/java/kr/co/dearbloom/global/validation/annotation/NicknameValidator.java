package kr.co.dearbloom.global.validation.annotation;

import kr.co.dearbloom.global.validation.ValidationPatterns;
import kr.co.dearbloom.global.validation.validatator.ValidNickname;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NicknameValidator implements ConstraintValidator<ValidNickname, String> {
    // 금지어 목록 (실제로는 DB나 설정 파일에서 관리)
    private static final String[] FORBIDDEN_WORDS = {
            "관리자", "admin", "운영자", "시스템",
            "운영", "관리", "마스터", "CEO", "CTO", "CFO",
            "dearbloom", "dear-bloom", "디어블룸", "디어 블룸",
            "Master", "Owner", "Editor", "Root"
    };

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // @NotNull과 함께 사용
        }

        // 1. 패턴 검증
        if (!ValidationPatterns.NICKNAME.matcher(value).matches()) {
            return false;
        }

        // 2. 금지어 검증
        String lowerValue = value.toLowerCase();
        for (String forbidden : FORBIDDEN_WORDS) {
            if (lowerValue.contains(forbidden.toLowerCase())) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        "사용할 수 없는 닉네임입니다"
                ).addConstraintViolation();
                return false;
            }
        }

        return true;
    }
}
