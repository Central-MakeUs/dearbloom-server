package kr.co.dearbloom.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import kr.co.dearbloom.domain.auth.dto.CodeExchangeRequest;
import kr.co.dearbloom.domain.auth.dto.TokenRefreshResponse;
import kr.co.dearbloom.domain.auth.facade.AuthFacade;
import kr.co.dearbloom.global.dto.response.ApiResponse;
import kr.co.dearbloom.global.dto.response.exception.CustomException;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import kr.co.dearbloom.global.swagger.ApiErrorCodes;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 하이브리드 로그인(로컬 웹 ↔ 개발 서버) 전용 컨트롤러. <b>비운영(!prod) 프로파일 한정.</b>
 *
 * <p>localhost 프론트는 개발 서버 도메인의 Set-Cookie 를 못 받는다. 그래서 소셜 로그인을
 * 두 단계로 나눈다:
 * <ul>
 *   <li>{@code GET /oauth2/local-entry} — 표식 쿠키 심고 구글 인증으로 진입 (구글 왕복 후 SuccessHandler 가 oneTimeCode 발급)</li>
 *   <li>{@code POST /api/auth/exchange} — 1회용 oneTimeCode 를 토큰으로 교환(응답 바디). 로컬 프론트가 자기 도메인 쿠키를 직접 심는다.</li>
 * </ul>
 * 둘 다 하이브리드 흐름 전용이라 한 컨트롤러에 모았다. (배포 웹은 백엔드가 직접 Set-Cookie 하므로 미사용)
 */
@Tag(name = "Social Login - Local Web", description = "로컬 웹 ↔ 개발 서버 하이브리드 로그인 (구글 전용, 개발용)")
@Profile("!prod")
@RestController
@RequiredArgsConstructor
public class SocialLoginLocalWebController {
    /** OAuth2SuccessHandler 와 공유하는 표식 쿠키 이름. */
    public static final String LOCAL_TARGET_COOKIE = "oauth2_local_target";
    private static final String ALLOWED_TARGET_PREFIX = "http://localhost:";

    private final AuthFacade authFacade;

    @GetMapping("/oauth2/local/entry")
    @Operation(summary = "하이브리드 로그인 진입", description = "구글만 가능. 표식 쿠키를 심고 구글 인증으로 리다이렉트합니다. target 은 localhost 만 허용.")
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

    /**
     * 하이브리드 로그인 전용 oneTimeCode → 토큰 교환.
     * localhost 는 백엔드 Set-Cookie 를 못 받으므로, 로컬 Next.js 서버가 이 API 로 토큰을
     * 응답 바디로 받아 자기 도메인 쿠키를 직접 심는다. code 는 1회용(30초 TTL).
     */
    @PostMapping("/oauth2/local/exchange")
    @Operation(summary = "oneTimeCode → 토큰 교환", description = "로컬 웹 <-> 개발 서버 간 1회용 oneTimeCode 를 access/refresh 토큰으로 교환합니다. (30초 TTL, 1회성)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "토큰 교환 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "oneTimeCode 가 없거나 만료됨(1회성)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "회원을 찾을 수 없음")
    })
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.MEMBER_NOT_FOUND})
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> exchange(
            @RequestBody @Valid CodeExchangeRequest request,
            HttpServletRequest httpRequest) {

        return ResponseEntity.ok(ApiResponse.success(
                authFacade.exchange(request.getOneTimeCode(), httpRequest)));
    }
}
