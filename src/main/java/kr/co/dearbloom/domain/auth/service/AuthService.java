package kr.co.dearbloom.domain.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.co.dearbloom.domain.auth.dto.TokenRefreshResponse;
import kr.co.dearbloom.domain.auth.entity.OAuthAccount;
import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.domain.member.service.MemberCommandService;
import kr.co.dearbloom.domain.member.service.MemberQueryService;
import kr.co.dearbloom.global.auth.jwt.TokenProvider;
import kr.co.dearbloom.global.properties.CookieProperties;
import kr.co.dearbloom.global.properties.JwtProperties;
import kr.co.dearbloom.global.util.HttpRequestUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * OAuth 인증 성공 후처리 공통 로직 (회원 확보 + 토큰 발급).
 * 웹 리다이렉트(OAuth2SuccessHandler)·네이티브(AuthFacade#nativeLogin) 양쪽에서 공용으로 쓴다.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    private final MemberQueryService memberQueryService;
    private final MemberCommandService memberCommandService;
    private final OAuthAccountService oAuthAccountService;
    private final TokenProvider tokenProvider;
    private final RefreshTokenSessionService refreshTokenSessionService;
    private final JwtProperties jwtProperties;
    private final CookieProperties cookieProperties;

    /** OAuthAccount 로 회원을 조회하고, 없으면 신규 가입 처리(Member 생성 + OAuthAccount 연결)까지 한다. */
    public Member findOrCreateMemberByOAuthAccount(OAuthAccount oauthAccount) {
        if (memberQueryService.notExistsByOauthAccount(oauthAccount)) {
            Member member = memberCommandService.createMember(oauthAccount);
            oAuthAccountService.linkMember(oauthAccount, member);
            return member;
        }
        return memberQueryService.getByOauthAccount(oauthAccount);
    }

    /**
     * 토큰 발급(#issueTokens) 후 access/refresh 를 HttpOnly 쿠키로 응답에 바로 심는다.
     * 일반 경로(dev 웹·운영) — 백엔드가 Set-Cookie 를 직접 내려도 브라우저가 받을 수 있는 케이스에서 사용.
     */
    public void issueTokensAndSetCookies(Member member, HttpServletRequest request, HttpServletResponse response) {
        TokenRefreshResponse tokens = issueTokens(member, request);
        addTokenCookie(response, ACCESS_TOKEN_COOKIE_NAME, tokens.getAccessToken(), jwtProperties.refreshTokenExpiry());
        addTokenCookie(response, REFRESH_TOKEN_COOKIE_NAME, tokens.getRefreshToken(), jwtProperties.refreshTokenExpiry());
    }

    /**
     * 토큰을 발급(+Redis 세션 저장)하되 쿠키는 심지 않고 값만 반환한다.
     * 하이브리드 로그인(localhost)에서 프론트가 응답 바디로 토큰을 받아 직접 쿠키를 심는 용도.
     */
    public TokenRefreshResponse issueTokens(Member member, HttpServletRequest request) {
        String ip = HttpRequestUtils.extractClientIp(request);
        String deviceInfo = request.getHeader("User-Agent");

        String refreshToken = tokenProvider.generateToken(member, jwtProperties.refreshTokenExpiry());
        refreshTokenSessionService.save(member, refreshToken, ip, deviceInfo);
        String accessToken = tokenProvider.generateToken(member, jwtProperties.accessTokenExpiry());

        return new TokenRefreshResponse(accessToken, refreshToken);
    }

    /** access/refresh 쿠키 공통 빌더. domain/secure/sameSite 는 CookieProperties(환경별 설정)를 따른다. */
    private void addTokenCookie(HttpServletResponse response, String name, String value, Duration maxAge) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(cookieProperties.isSecure())
                .sameSite(cookieProperties.getSameSite())
                .path("/")
                .maxAge(maxAge);
        if (cookieProperties.getDomain() != null && !cookieProperties.getDomain().isBlank()) {
            builder.domain(cookieProperties.getDomain());
        }
        response.addHeader(HttpHeaders.SET_COOKIE, builder.build().toString());
    }
}
