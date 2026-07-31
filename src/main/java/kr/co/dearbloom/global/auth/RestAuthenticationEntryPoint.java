package kr.co.dearbloom.global.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * 인증이 필요한 경로에 비인증으로 접근했을 때 401 을 {@link kr.co.dearbloom.global.dto.response.ApiResponse}
 * 형태로 내려준다. {@code HttpStatusEntryPoint} 는 본문 없이 상태코드만 보내서 프론트 에러 처리가 깨진다.
 *
 * <p>토큰이 <b>있는데</b> 만료/무효인 경우는 {@code TokenAuthenticationFilter} 가 먼저 걸러 각각의 코드를
 * 내려주므로, 여기까지 오는 건 사실상 토큰이 아예 없는 요청이다 — {@code @CurrentCustomer} 등 리졸버가
 * SecurityContext 가 빈 것을 보고 던지던 것과 같은 INVALID_TOKEN 으로 맞춘다.</p>
 */
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        ApiErrorResponseWriter.write(response, objectMapper, ErrorCode.INVALID_TOKEN);
    }
}
