package kr.co.dearbloom.global.validation.validatator;

import kr.co.dearbloom.global.validation.annotation.NicknameValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = NicknameValidator.class)
public @interface ValidNickname {
    String message() default "닉네임은 2-12자의 한글, 영문, 숫자만 가능합니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}