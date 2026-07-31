package kr.co.dearbloom.global.auth;

import jakarta.servlet.http.HttpServletResponse;
import kr.co.dearbloom.global.dto.response.ApiResponse;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import kr.co.dearbloom.global.dto.response.exception.ErrorDetail;
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * 시큐리티 필터 체인에서 발생한 인증 실패를 {@link ApiResponse} 형태로 직접 기록한다.
 * 필터/EntryPoint 는 {@code @RestControllerAdvice} 보다 앞단이라 예외 핸들러를 타지 못하므로,
 * 컨트롤러에서 나가는 에러와 응답 포맷을 맞추려면 여기서 같은 방식으로 써야 한다.
 */
public final class ApiErrorResponseWriter {
    private ApiErrorResponseWriter() {}

    public static void write(HttpServletResponse response, ObjectMapper objectMapper, ErrorCode errorCode)
            throws IOException {
        ApiResponse<?> body = ApiResponse.error(new ErrorDetail(errorCode.getCode(), errorCode.getMessage()));
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
