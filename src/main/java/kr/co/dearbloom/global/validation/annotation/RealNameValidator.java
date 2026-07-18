package kr.co.dearbloom.global.validation.annotation;

import kr.co.dearbloom.global.validation.ValidationPatterns;
import kr.co.dearbloom.global.validation.validatator.ValidRealName;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RealNameValidator implements ConstraintValidator<ValidRealName, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // @NotBlank 과 함께 사용
        }
        return ValidationPatterns.REAL_NAME.matcher(value).matches();
    }
}
