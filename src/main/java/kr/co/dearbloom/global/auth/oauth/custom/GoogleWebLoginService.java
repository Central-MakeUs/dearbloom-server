package kr.co.dearbloom.global.auth.oauth.custom;

import jakarta.servlet.http.HttpServletResponse;
import kr.co.dearbloom.domain.member.entity.MemberRole;
import kr.co.dearbloom.global.auth.oauth.SignupRoleCookie;
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
 * <p>진입 때 고른 고객/작가 role 을 <b>signup_role 쿠키</b>에 심어 구글로 넘긴 뒤,
 * {@code OAuth2SuccessHandler} 에서 회수해 토큰 activeRole·온보딩 라우팅에 반영한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleWebLoginService {
    /** Spring Security 가 처리하는 실제 구글 OAuth 진입 경로. 이 커스텀 진입점은 여기로 위임한다. */
    private static final String SPRING_SECURITY_GOOGLE_AUTHORIZE = "/oauth2/authorization/google";

    /**
     * 구글 웹 로그인 진입 → 고른 role 을 signup_role 쿠키에 심고 Spring Security 진입 경로로 위임할 리다이렉트 대상을 반환한다.
     */
    public String resolveEntryRedirect(MemberRole role, HttpServletResponse response) {
        SignupRoleCookie.write(response, role);
        return SPRING_SECURITY_GOOGLE_AUTHORIZE;
    }
}
