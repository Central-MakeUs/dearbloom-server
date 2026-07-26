package kr.co.dearbloom.global.auth.oauth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.co.dearbloom.domain.member.entity.MemberRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

import java.time.Duration;
import java.util.Arrays;

/**
 * 웹 소셜 로그인에서 <b>진입 시점에 고른 role(고객/작가)</b>을 콜백까지 실어 나르는 임시 쿠키.
 *
 * <p>웹은 진입(authorize) → 소셜 왕복 → 콜백(OAuth2SuccessHandler / Apple form_post) 사이에
 * 값을 넘길 방법이 없어, 진입 때 이 쿠키에 role 을 심고 콜백에서 회수해 온보딩 라우팅에 쓴다.
 * 애플 form_post 는 cross-site POST 라 SameSite=None; Secure 여야 쿠키가 실려온다(구글도 동일 정책 재사용).
 */
@Slf4j
public final class SignupRoleCookie {
    public static final String NAME = "signup_role";
    private SignupRoleCookie() {
    }

    /** 진입 시점에 고른 role 을 쿠키에 심는다. */
    public static void write(HttpServletResponse response, MemberRole role) {
        if (role == null) {
            return;
        }
        ResponseCookie cookie = ResponseCookie.from(NAME, role.name())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(Duration.ofMinutes(5))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /** 콜백에서 role 을 회수한다. 없거나 값이 이상하면 null. */
    public static MemberRole read(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        String value = Arrays.stream(request.getCookies())
                .filter(c -> NAME.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return MemberRole.valueOf(value);
        } catch (IllegalArgumentException e) {
            log.warn("[SignupRoleCookie] 알 수 없는 role 값: {}", value);
            return null;
        }
    }

    /** 회수 후 쿠키를 만료시킨다. */
    public static void clear(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(NAME, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
