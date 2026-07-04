package kr.co.dearbloom.global.auth.oauth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import kr.co.dearbloom.global.dto.response.exception.CustomException;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 로컬 웹 ↔ 개발 서버 하이브리드 로그인 진입점. <b>dev 프로파일 한정.</b>
 *
 * <p>로컬 프론트(localhost) 개발자는 소셜로그인 시작을 이 엔드포인트로 한다:
 * <pre>GET /oauth2/local-entry?target=http://localhost:4000/api/auth/callback</pre>
 *
 * <p>target 을 host-only 표식 쿠키에 담고 구글 인증으로 리다이렉트한다. 이후 Google 왕복 내내
 * 브라우저는 dev-api 에 머물러 이 쿠키가 살아있고, {@link OAuth2SuccessHandler} 가 로그인 성사 시
 * 이 쿠키 유무로 "일반 vs 하이브리드" 분기를 판단한다. code 교환은 {@code POST /api/auth/exchange}(AuthController) 담당.
 *
 * <p>target 은 오픈 리다이렉트 방지를 위해 {@code http://localhost:} 로만 제한한다.
 */
@Tag(name = "OAuth - Local Entry", description = "로컬 웹 ↔ 개발 서버 하이브리드 로그인 진입점 (dev 전용)")
@Profile("dev")
@RestController
@RequiredArgsConstructor
public class OAuthLocalEntryController {

    /** OAuth2SuccessHandler 와 공유하는 표식 쿠키 이름. */
    public static final String LOCAL_TARGET_COOKIE = "oauth2_local_target";

    private static final String ALLOWED_TARGET_PREFIX = "http://localhost:";

    @GetMapping("/oauth2/local-entry")
    @Operation(summary = "하이브리드 로그인 진입점", description = "표식 쿠키를 심고 구글 인증으로 리다이렉트합니다. target 은 localhost 만 허용.")
    public void localEntry(@RequestParam String target, HttpServletResponse response) throws IOException {
        if (!target.startsWith(ALLOWED_TARGET_PREFIX)) {
            throw new CustomException(ErrorCode.PARAMETER_BAD_REQUEST);
        }

        Cookie cookie = new Cookie(LOCAL_TARGET_COOKIE, URLEncoder.encode(target, StandardCharsets.UTF_8));
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(300); // OAuth 왕복 동안만 (5분)
        response.addCookie(cookie);

        response.sendRedirect("/oauth2/authorization/google");
    }
}
