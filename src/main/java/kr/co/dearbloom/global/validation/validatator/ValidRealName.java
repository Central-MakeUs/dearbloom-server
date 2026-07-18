package kr.co.dearbloom.global.validation.validatator;

import kr.co.dearbloom.global.validation.annotation.RealNameValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = RealNameValidator.class)
public @interface ValidRealName {
    String message() default "이름은 2-5자의 한글 또는 영문만 가능합니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
