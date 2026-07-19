package kr.co.dearbloom.domain.auth.facade;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.co.dearbloom.domain.auth.dto.TokenRefreshResponse;
import kr.co.dearbloom.domain.auth.entity.OAuthAccount;
import kr.co.dearbloom.domain.auth.dto.NativeLoginRequest;
import kr.co.dearbloom.domain.auth.dto.SocialUserInfo;
import kr.co.dearbloom.domain.auth.entity.OAuthProvider;
import kr.co.dearbloom.domain.auth.service.AuthService;
import kr.co.dearbloom.domain.auth.service.custom.AppleNativeAuthService;
import kr.co.dearbloom.domain.auth.service.custom.GoogleNativeAuthService;
import kr.co.dearbloom.domain.auth.service.OAuthAccountService;
import kr.co.dearbloom.domain.auth.service.TokenService;
import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.domain.member.service.MemberQueryService;
import kr.co.dearbloom.domain.auth.service.OAuthOneTimeCodeService;
import kr.co.dearbloom.global.auth.oauth.custom.AppleWebLoginService;
import kr.co.dearbloom.global.auth.oauth.custom.GoogleWebLoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthFacade {
    private final MemberQueryService memberQueryService;
    private final OAuthAccountService oAuthAccountService;
    private final AuthService authService;
    private final TokenService tokenService;
    private final GoogleNativeAuthService googleNativeAuthService;
    private final AppleNativeAuthService appleNativeAuthService;
    private final OAuthOneTimeCodeService oAuthOneTimeCodeService;
    private final AppleWebLoginService appleWebLoginService;
    private final GoogleWebLoginService googleWebLoginService;

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
     * 성공 시 기존 redirect OAuth와 동일한 HttpOnly 쿠키를 설정한다.
     */
    public void nativeLogin(NativeLoginRequest request,
                             HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        OAuthAccount oauthAccount = switch (request.getSocialProvider()) {
            case GOOGLE -> {
                SocialUserInfo userInfo = googleNativeAuthService.exchangeServerAuthCode(request.getSocialToken());
                yield oAuthAccountService.findOrCreateNativeAccount(OAuthProvider.GOOGLE, userInfo);
            }
            case APPLE -> {
                SocialUserInfo userInfo = appleNativeAuthService.verifyIdentityToken(request.getSocialToken());
                yield oAuthAccountService.findOrCreateNativeAccount(OAuthProvider.APPLE, userInfo);
            }
        };

        Member member = authService.findOrCreateMemberByOAuthAccount(oauthAccount);
        authService.issueTokensAndSetCookies(member, httpRequest, httpResponse);
    }

    /** 애플 웹 로그인 진입 — 애플 인증 URL 생성 + state 쿠키 심기. (AppleWebLoginService 위임) */
    public String appleWebAuthorizeUrl(HttpServletResponse response) {
        return appleWebLoginService.createAuthorizeUrl(response);
    }

    /** 애플 웹 로그인 콜백 처리 — id_token 검증 → 회원 처리 → 쿠키 발급 후 리다이렉트 대상 반환. (위임) */
    public String handleAppleWebCallback(String idToken, String state, String error,
                                         HttpServletRequest request, HttpServletResponse response) {
        return appleWebLoginService.handleCallback(idToken, state, error, request, response);
    }

    /** 구글 웹 로그인 진입 — Spring Security 진입 경로로 위임할 리다이렉트 대상 반환. (GoogleWebLoginService 위임) */
    public String googleWebAuthorizeRedirect(HttpServletResponse response) {
        return googleWebLoginService.resolveEntryRedirect(response);
    }
}
