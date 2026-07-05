package kr.co.dearbloom.global.swagger;

import kr.co.dearbloom.global.dto.response.exception.ErrorCode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 이 API 가 응답할 수 있는 에러코드들을 나열한다.
 * {@link ApiErrorCodesCustomizer} 가 나열된 코드를 HTTP 상태코드별로 그룹핑해
 * Swagger 응답(상태코드마다 코드별 예시 포함)을 자동 생성한다.
 *
 * <pre>
 * &#64;ApiErrorCodes({ErrorCode.MEMBER_NOT_FOUND, ErrorCode.INVALID_TOKEN, ErrorCode.NICKNAME_ALREADY_EXISTS})
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiErrorCodes {
    ErrorCode[] value();
}
