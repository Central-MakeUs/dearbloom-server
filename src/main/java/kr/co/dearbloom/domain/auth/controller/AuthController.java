package kr.co.dearbloom.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import kr.co.dearbloom.domain.auth.dto.CodeExchangeRequest;
import kr.co.dearbloom.domain.auth.dto.NativeLoginRequest;
import kr.co.dearbloom.domain.auth.dto.TokenRefreshRequest;
import kr.co.dearbloom.domain.auth.dto.TokenRefreshResponse;
import kr.co.dearbloom.domain.auth.facade.AuthFacade;
import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.global.dto.response.ApiResponse;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import kr.co.dearbloom.global.swagger.ApiErrorCodes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "인증 API")
public class AuthController {

    private final AuthFacade authFacade;

    @PostMapping("/refresh")
    @Operation(summary = "accessToken 재발급", description = "refreshToken 검증 후 새 accessToken 발급")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201", description = "accessToken 재발급 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "refreshToken 이 유효하지 않음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "회원을 찾을 수 없음")
    })
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.MEMBER_NOT_FOUND})
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> createNewAccessToken(@RequestBody TokenRefreshRequest request) {
        TokenRefreshResponse response = authFacade.refresh(request.getRefreshToken());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * 하이브리드 로그인(로컬 웹 ↔ 개발 서버) 전용 oneTimeCode → 토큰 교환.
     * localhost 는 백엔드 Set-Cookie 를 못 받으므로, 로컬 Next.js 서버가 이 API 로 토큰을
     * 응답 바디로 받아 자기 도메인 쿠키를 직접 심는다. code 는 1회용(30초 TTL).
     */
    @PostMapping("/exchange")
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

    /**
     * 네이티브 앱(WebView)에서 소셜 SDK로 얻은 토큰으로 로그인.
     * - Google: serverAuthCode (offlineAccess=true 로 획득)
     * 성공 시 기존 redirect OAuth와 동일한 HttpOnly 쿠키를 설정하고 200 반환.
     */
    @PostMapping("/login")
    @Operation(summary = "네이티브 소셜 로그인", description = "WebView 앱에서 네이티브 SDK 로 얻은 토큰으로 로그인합니다. provider 는 요청 본문으로 전달합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "로그인 성공 (HttpOnly 쿠키 설정)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "지원하지 않는 provider"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "소셜 로그인 토큰이 유효하지 않음")
    })
    @ApiErrorCodes({ErrorCode.UNSUPPORTED_OAUTH_PROVIDER, ErrorCode.INVALID_OAUTH_TOKEN})
    public ResponseEntity<ApiResponse<Void>> nativeLogin(
            @RequestBody @Valid NativeLoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        authFacade.nativeLogin(request.getProvider(), request.getToken(), httpRequest, httpResponse);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @DeleteMapping("/logout")
    @Operation(summary = "로그아웃", description = "리프레시 토큰 세션을 삭제해 무효화합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "로그아웃 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "인증 필요 (토큰 없음/만료/유효하지 않음)")
    })
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN})
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal Member member) {
        authFacade.logout(member.getMemberId());
        return ResponseEntity.ok(ApiResponse.success());
    }
}
