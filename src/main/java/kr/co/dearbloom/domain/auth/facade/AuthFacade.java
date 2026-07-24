package kr.co.dearbloom.domain.auth.facade;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.co.dearbloom.domain.auth.dto.TokenRefreshResponse;
import kr.co.dearbloom.domain.auth.entity.OAuthAccount;
import kr.co.dearbloom.domain.auth.dto.NativeLoginRequest;
import kr.co.dearbloom.domain.auth.dto.SocialLoginResponse;
import kr.co.dearbloom.domain.auth.dto.SocialUserInfo;
import kr.co.dearbloom.domain.auth.entity.OAuthProvider;
import kr.co.dearbloom.domain.member.entity.MemberRole;
import kr.co.dearbloom.domain.auth.service.AuthService;
import kr.co.dearbloom.domain.auth.service.custom.AppleNativeAuthService;
import kr.co.dearbloom.domain.auth.service.custom.AppleTokenService;
import kr.co.dearbloom.domain.auth.service.custom.GoogleNativeAuthService;
import kr.co.dearbloom.domain.auth.service.OAuthAccountService;
import kr.co.dearbloom.domain.auth.service.TokenService;
import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.domain.member.service.MemberQueryService;
import kr.co.dearbloom.domain.auth.service.OAuthOneTimeCodeService;
import kr.co.dearbloom.global.auth.oauth.custom.AppleWebLoginService;
import kr.co.dearbloom.global.auth.oauth.custom.GoogleWebLoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthFacade {
    private final MemberQueryService memberQueryService;
    private final OAuthAccountService oAuthAccountService;
    private final AuthService authService;
    private final GoogleNativeAuthService googleNativeAuthService;
    private final AppleNativeAuthService appleNativeAuthService;
    private final AppleTokenService appleTokenService;
    private final OAuthOneTimeCodeService oAuthOneTimeCodeService;
    private final AppleWebLoginService appleWebLoginService;
    private final GoogleWebLoginService googleWebLoginService;

    @Value("${apple.native.client-id:}")
    private String appleNativeClientId;

    /**
     * 하이브리드 로그인(로컬 웹 ↔ 개발 서버) 전용 oneTimeCode → 토큰 교환.
     * localhost 는 백엔드 Set-Cookie 를 못 받으므로, 로컬 Next.js 서버가 이 API 로 토큰을
     * 응답 바디로 받아 자기 도메인 쿠키를 직접 심는다. code 는 1회용(30초 TTL).
     */
    public TokenRefreshResponse exchange(String oneTimeCode, HttpServletRequest httpRequest) {
        Long memberId = oAuthOneTimeCodeService.consume(oneTimeCode);
        Member member = memberQueryService.getByMemberIdOrThrow(memberId);
        return authService.issueTokens(member, httpRequest);
    }

    /**
     * 네이티브 앱(WebView)에서 소셜 SDK로 얻은 토큰으로 로그인.
     * - Google: serverAuthCode (offlineAccess=true 로 획득)
     * 성공 시 기존 redirect OAuth와 동일한 HttpOnly 쿠키를 설정하고, 온보딩 라우팅 정보를 반환한다.
     */
    public SocialLoginResponse nativeLogin(NativeLoginRequest request,
                                           HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        OAuthAccount oauthAccount = switch (request.getSocialProvider()) {
            case GOOGLE -> {
                SocialUserInfo userInfo = googleNativeAuthService.exchangeServerAuthCode(request.getSocialToken());
                yield oAuthAccountService.findOrCreateNativeAccount(OAuthProvider.GOOGLE, userInfo);
            }
            case APPLE -> {
                SocialUserInfo userInfo = appleNativeAuthService.verifyIdentityToken(request.getSocialToken());
                OAuthAccount account = oAuthAccountService.findOrCreateNativeAccount(OAuthProvider.APPLE, userInfo);
                // 탈퇴 revoke 용: authorizationCode 를 refresh token 으로 교환해 저장(App Store 필수).
                if (request.getAuthorizationCode() != null && !request.getAuthorizationCode().isBlank()) {
                    String refreshToken = appleTokenService.exchangeAuthorizationCode(
                            request.getAuthorizationCode(), appleNativeClientId, null);
                    if (refreshToken != null) {
                        oAuthAccountService.updateRefreshToken(account, refreshToken, appleNativeClientId);
                    }
                }
                yield account;
            }
        };

        MemberRole selectedRole = request.getRole();
        Member member = authService.findOrCreateMemberByOAuthAccount(oauthAccount);
        MemberRole overrideActiveRole = authService.resolveActiveRoleForLogin(member, selectedRole);
        authService.issueTokensAndSetCookies(member, overrideActiveRole, httpRequest, httpResponse);
        return SocialLoginResponse.of(member, selectedRole);
    }

    /** 애플 웹 로그인 진입 — 애플 인증 URL 생성 + state/signup_role 쿠키 심기. (AppleWebLoginService 위임) */
    public String appleWebAuthorizeUrl(MemberRole role, HttpServletResponse response) {
        return appleWebLoginService.createAuthorizeUrl(role, response);
    }

    /** 애플 웹 로그인 콜백 처리 — id_token 검증 → 회원 처리 → 쿠키 발급 후 리다이렉트 대상 반환. (위임) */
    public String handleAppleWebCallback(String idToken, String code, String state, String error,
                                         HttpServletRequest request, HttpServletResponse response) {
        return appleWebLoginService.handleCallback(idToken, code, state, error, request, response);
    }

    /** 구글 웹 로그인 진입 — signup_role 쿠키를 심고 Spring Security 진입 경로로 위임할 리다이렉트 대상 반환. (위임) */
    public String googleWebAuthorizeRedirect(MemberRole role, HttpServletResponse response) {
        return googleWebLoginService.resolveEntryRedirect(role, response);
    }
}
