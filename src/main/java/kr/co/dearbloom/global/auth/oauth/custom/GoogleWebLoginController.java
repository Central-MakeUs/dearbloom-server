package kr.co.dearbloom.global.auth.oauth.custom;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * 구글 웹 로그인 진입점(커스텀). 실제 로그인은 Spring Security 가 처리하고, 이 진입점은 그 앞단에서
 * (추후 role 쿠키 등을 심고) {@code /oauth2/authorization/google} 로 위임만 한다.
 *
 * <p>경로는 애플 웹({@code /oauth2/apple/authorize})과 대칭으로 {@code /oauth2/google/authorize} 를 쓴다.
 */
@RequiredArgsConstructor
@RestController
@Tag(name = "Google OAuth Web Login", description = "구글 웹 로그인 진입점")
@Hidden
public class GoogleWebLoginController {
    private final GoogleWebLoginService googleWebLoginService;

    /** 구글 웹 로그인 진입 → Spring Security 진입 경로로 리다이렉트. */
    @GetMapping("/oauth2/google/authorize")
    public void authorize(HttpServletResponse response) throws IOException {
        response.sendRedirect(googleWebLoginService.resolveEntryRedirect(response));
    }
}
