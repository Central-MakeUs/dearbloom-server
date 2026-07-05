package kr.co.dearbloom.global.auth.oauth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import kr.co.dearbloom.global.dto.response.exception.CustomException;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import kr.co.dearbloom.global.swagger.ApiErrorCodes;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Tag(name = "OAuth - Local Entry", description = "로컬 웹 ↔ 개발 서버 하이브리드 로그인 경로")
@Profile("!prod")
@RestController
@RequiredArgsConstructor
public class OAuthLocalEntryController {

    /** OAuth2SuccessHandler 와 공유하는 표식 쿠키 이름. */
    public static final String LOCAL_TARGET_COOKIE = "oauth2_local_target";

    private static final String ALLOWED_TARGET_PREFIX = "http://localhost:";

    @GetMapping("/oauth2/local-entry")
    @Operation(summary = "하이브리드 로그인 요청", description = "표식 쿠키를 심고 구글 인증으로 리다이렉트합니다. target 은 localhost 만 허용.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "302", description = "구글 인증으로 리다이렉트 (표식 쿠키 설정)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "target 이 localhost 로 시작하지 않음")
    })
    @ApiErrorCodes(ErrorCode.PARAMETER_BAD_REQUEST)
    public void localEntry(
            @Parameter(description = "인증 완료 후 돌아갈 대상 URL. http://localhost: 로 시작해야 함", example = "http://localhost:3000/api/auth/callback")
            @RequestParam String target,
            HttpServletResponse response) throws IOException {
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
