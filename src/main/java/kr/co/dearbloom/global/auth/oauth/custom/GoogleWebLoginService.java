package kr.co.dearbloom.global.auth.oauth.custom;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 구글 웹 로그인 진입점 처리.
 *
 * <p>애플 웹과 달리 구글 웹 로그인 자체는 <b>Spring Security oauth2Login</b>이 처리한다
 * ({@code /oauth2/authorization/google} → 구글 왕복 → {@code /login/oauth2/code/google} 콜백 → OAuth2SuccessHandler).
 * 그런데 Spring Security 기본 진입 경로엔 우리가 값을 실어보낼 방법이 없어서, 그 <b>앞단</b>에 이 커스텀 진입점을 둔다.
 *
 * <p>지금은 role 미반영이라 Spring Security 진입 경로로 그대로 위임(리다이렉트)만 한다.
 * 추후 가입 시 고객/작가 role 을 여기서 받아 <b>signup_role 쿠키</b>에 심고 구글로 넘긴 뒤,
 * {@code OAuth2SuccessHandler} 에서 회수해 신규 회원 생성에 반영할 예정이다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleWebLoginService {
    /** Spring Security 가 처리하는 실제 구글 OAuth 진입 경로. 이 커스텀 진입점은 여기로 위임한다. */
    private static final String SPRING_SECURITY_GOOGLE_AUTHORIZE = "/oauth2/authorization/google";

    /**
     * 구글 웹 로그인 진입 → Spring Security 진입 경로로 위임할 리다이렉트 대상을 반환한다.
     * TODO(role): 가입 시 role(CUSTOMER/ARTIST)을 받아 signup_role 쿠키를 {@code response} 에 심을 것.
     */
    public String resolveEntryRedirect(HttpServletResponse response) {
        return SPRING_SECURITY_GOOGLE_AUTHORIZE;
    }
}
