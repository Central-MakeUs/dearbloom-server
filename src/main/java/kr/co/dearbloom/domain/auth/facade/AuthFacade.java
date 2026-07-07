package kr.co.dearbloom.domain.auth.facade;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.co.dearbloom.domain.auth.dto.TokenRefreshResponse;
import kr.co.dearbloom.domain.auth.entity.OAuthAccount;
import kr.co.dearbloom.domain.auth.dto.NativeLoginRequest;
import kr.co.dearbloom.domain.auth.entity.OAuthProvider;
import kr.co.dearbloom.domain.auth.service.AuthService;
import kr.co.dearbloom.domain.auth.service.GoogleNativeAuthService;
import kr.co.dearbloom.domain.auth.service.OAuthAccountService;
import kr.co.dearbloom.domain.auth.service.TokenService;
import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.domain.member.service.MemberQueryService;
import kr.co.dearbloom.global.auth.jwt.TokenProvider;
import kr.co.dearbloom.domain.auth.service.OAuthOneTimeCodeService;
import kr.co.dearbloom.global.dto.response.exception.CustomException;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthFacade {

    private final MemberQueryService memberQueryService;
    private final OAuthAccountService oAuthAccountService;
    private final AuthService authService;
    private final TokenService tokenService;
    private final TokenProvider tokenProvider;
    private final GoogleNativeAuthService googleNativeAuthService;
    private final OAuthOneTimeCodeService oAuthOneTimeCodeService;

    /**
     * accessToken 재발급. 회전(rotation) 미구현이라 refreshToken 자체는 그대로 돌려준다.
     * (RefreshTokenSessionService 의 rotation 전용 메서드가 주석 처리된 상태를 그대로 유지)
     */
    public TokenRefreshResponse refresh(String refreshToken) {
        if (!tokenProvider.validToken(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        Long memberId = tokenProvider.getMemberId(refreshToken);
        Member member = memberQueryService.getByMemberIdOrThrow(memberId);
        String newAccessToken = tokenService.createAccessToken(member);

        return new TokenRefreshResponse(newAccessToken, refreshToken);
    }

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

    public void logout(Long memberId) {
        tokenService.logout(memberId);
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
                GoogleNativeAuthService.GoogleUserInfo userInfo =
                        googleNativeAuthService.exchangeServerAuthCode(request.getSocialToken());
                yield oAuthAccountService.findOrCreateNativeAccount(
                        OAuthProvider.GOOGLE, userInfo.sub(), userInfo.email(), userInfo.name());
            }
            default -> throw new CustomException(ErrorCode.UNSUPPORTED_OAUTH_PROVIDER);
        };

        Member member = authService.findOrCreateMemberByOAuthAccount(oauthAccount);
        authService.issueTokensAndSetCookies(member, httpRequest, httpResponse);
    }
}
