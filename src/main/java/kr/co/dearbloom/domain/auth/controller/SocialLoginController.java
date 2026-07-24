package kr.co.dearbloom.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import kr.co.dearbloom.domain.auth.dto.NativeLoginRequest;
import kr.co.dearbloom.domain.auth.dto.SocialLoginResponse;
import kr.co.dearbloom.domain.auth.facade.AuthFacade;
import kr.co.dearbloom.domain.member.entity.MemberRole;
import kr.co.dearbloom.global.dto.response.ApiResponse;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import kr.co.dearbloom.global.swagger.ApiErrorCodes;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * 소셜 로그인 진입점 통합 컨트롤러 (구글·애플 / 네이티브·웹).
 *
 * <p>성격이 다른 두 부류가 함께 있다:
 * <ul>
 *   <li><b>네이티브</b> — {@code POST /api/auth/login} : 앱이 SDK 로 받은 토큰을 JSON 으로 넘겨 로그인 (AuthFacade 위임)</li>
 *   <li><b>웹(리다이렉트)</b> — {@code /oauth2/{provider}/*} : 브라우저 리다이렉트 방식 진입점 (각 Web 서비스 위임)</li>
 * </ul>
 * 실제 로직은 {@link AuthFacade} 를 통해 위임한다(네이티브·웹 모두 facade 단일 진입).
 */
@RequiredArgsConstructor
@RestController
@Tag(name = "Social Login", description = "소셜 로그인 (구글·애플 / 앱·웹)")
public class SocialLoginController {
    private final AuthFacade authFacade;

    // ─── 네이티브 (앱 WebView) ─────────────────────────────────────────────

    /**
     * 네이티브 앱(WebView)에서 소셜 SDK로 얻은 토큰으로 로그인.
     * - Google: serverAuthCode (offlineAccess=true 로 획득) → 서버가 토큰 교환
     * - Apple: identityToken(JWT) → 서버가 Apple JWKS 로 검증
     * 성공 시 기존 redirect OAuth와 동일한 HttpOnly 쿠키를 설정하고 200 반환.
     */
    @PostMapping("/api/auth/login")
    @Operation(summary = "네이티브 소셜 로그인 (구글, 애플 공용)",
            description = "앱(WebView)에서 네이티브 소셜 SDK 로 로그인할 때 쓰는 <b>단일 엔드포인트</b>입니다. "
                    + "웹 브라우저 리다이렉트 로그인이 아니라, 앱이 SDK 로 이미 받은 토큰을 서버로 넘기는 방식입니다."
                    + "<br><br><b>요청 본문(JSON)</b>"
                    + "<br>• <b>socialProvider</b>: 소셜 종류. <code>GOOGLE</code> 또는 <code>APPLE</code>"
                    + "<br>• <b>socialToken</b>: 아래처럼 provider 별로 <b>넣는 값이 다릅니다.</b>"
                    + "<br>&nbsp;&nbsp;- <b>GOOGLE</b> → 구글 네이티브 SDK 로그인 결과의 <code>serverAuthCode</code> "
                    + "(offlineAccess=true 로 얻는 서버용 인가코드). 서버가 이 코드를 구글에 토큰으로 교환해 사용자 정보를 얻습니다."
                    + "<br>&nbsp;&nbsp;- <b>APPLE</b> → 애플 네이티브 SDK(Sign in with Apple) 로그인 결과의 <code>identityToken</code> "
                    + "(애플이 서명한 JWT). 서버가 애플 공개키(JWKS)로 검증해 사용자 정보를 얻습니다."
                    + "<br>• <b>role</b>: 로그인 화면에서 고른 <code>CUSTOMER</code>(고객) 또는 <code>ARTIST</code>(작가). 온보딩 라우팅에 사용합니다."
                    + "<br><br><b>응답</b>: 성공 시 accessToken/refreshToken 을 <b>HttpOnly 쿠키</b>로 설정하고, 본문으로 "
                    + "온보딩 라우팅 정보(selectedRole·hasCustomer·hasArtist·needsOnboarding)를 반환합니다."
                    + "<br>선택한 role 의 프로필이 아직 없으면 <code>needsOnboarding=true</code> 이며, 이때 토큰 activeRole 은 "
                    + "강제되지 않습니다(기본 규칙 적용). 프론트는 needsOnboarding 이 true 면 selectedRole 에 맞는 온보딩 화면으로 보냅니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "로그인 성공 (HttpOnly 쿠키 설정 + 온보딩 라우팅 정보 반환)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "지원하지 않는 provider"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "소셜 로그인 토큰이 유효하지 않음")
    })
    @ApiErrorCodes({ErrorCode.UNSUPPORTED_OAUTH_PROVIDER, ErrorCode.INVALID_OAUTH_TOKEN})
    public ResponseEntity<ApiResponse<SocialLoginResponse>> nativeLogin(
            @RequestBody @Valid NativeLoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        SocialLoginResponse result = authFacade.nativeLogin(request, httpRequest, httpResponse);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ─── 웹 (브라우저 리다이렉트) ───────────────────────────────────────────

    /** 애플 웹 로그인 진입 → 애플 인증 페이지로 리다이렉트 (state·signup_role 쿠키 심음). */
    @GetMapping("/oauth2/apple/authorize")
    @Operation(summary = "애플 웹 로그인 진입",
            description = "role 쿼리파라미터로 로그인 화면에서 고른 CUSTOMER/ARTIST 를 넘깁니다. "
                    + "이 값은 signup_role 쿠키로 실려 콜백까지 전달되며, 로그인 성공 후 프론트 콜백 URL 에 "
                    + "role·needsOnboarding 쿼리파라미터로 반환됩니다.")
    public void appleAuthorize(@RequestParam MemberRole role, HttpServletResponse response) throws IOException {
        response.sendRedirect(authFacade.appleWebAuthorizeUrl(role, response));
    }

    /** 애플이 form_post 로 보내는 콜백. 프론트가 직접 호출하지 않으므로 Swagger 에서 숨김. */
    @PostMapping("/oauth2/apple/callback")
    @Operation(hidden = true, summary = "애플 웹 로그인 콜백")
    public void appleCallback(
            @RequestParam(name = "id_token", required = false) String idToken,
            @RequestParam(name = "code", required = false) String code,
            @RequestParam(name = "state", required = false) String state,
            @RequestParam(name = "error", required = false) String error,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        response.sendRedirect(authFacade.handleAppleWebCallback(idToken, code, state, error, request, response));
    }

    /** 구글 웹 로그인 진입 → Spring Security 진입 경로로 위임 리다이렉트 (signup_role 쿠키 심음). */
    @GetMapping("/oauth2/google/authorize")
    @Operation(summary = "구글 웹 로그인 진입",
            description = "role 쿼리파라미터로 로그인 화면에서 고른 CUSTOMER/ARTIST 를 넘깁니다. "
                    + "이 값은 signup_role 쿠키로 실려 콜백까지 전달되며, 로그인 성공 후 프론트 콜백 URL 에 "
                    + "role·needsOnboarding 쿼리파라미터로 반환됩니다.")
    public void googleAuthorize(@RequestParam MemberRole role, HttpServletResponse response) throws IOException {
        response.sendRedirect(authFacade.googleWebAuthorizeRedirect(role, response));
    }
}
