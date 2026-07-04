package kr.co.dearbloom.global.auth.oauth;

import kr.co.dearbloom.domain.member.entity.OAuthAccount;
import kr.co.dearbloom.domain.member.service.OAuthAccountService;
import kr.co.dearbloom.domain.member.service.RefreshTokenSessionService;
import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.domain.member.service.MemberService;
import kr.co.dearbloom.global.auth.jwt.TokenProvider;
import kr.co.dearbloom.global.properties.CookieProperties;
import kr.co.dearbloom.global.properties.JwtProperties;
import kr.co.dearbloom.global.util.HttpRequestUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

@Slf4j
@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler { // 인증 성공시 실행할 핸들러
    @Value("${url.oauth-callback}")
    private String REDIRECT_PATH;

    private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    private final TokenProvider tokenProvider;
    private final RefreshTokenSessionService refreshTokenSessionService;
    private final OAuth2AuthorizationRequestBasedOnCookieRepository authorizationRequestRepository;
    private final MemberService memberService;
    private final OAuthAccountService oAuthAccountService;
    private final JwtProperties jwtProperties;
    private final CookieProperties cookieProperties;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // OAuth2UserCustomService의 loadUser()에서 반환한 OAuth2User 사용
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // CustomOAuth2User로 캐스팅
        if (!(oAuth2User instanceof CustomOAuth2User)) {
            throw new IllegalArgumentException("OAuth2User is not an instance of CustomOAuth2User");
        }
        CustomOAuth2User customUser = (CustomOAuth2User) oAuth2User;
        OAuthAccount oauthAccount = customUser.getOauthAccount();

        /**
         * 최초 가입시
         * 1. Member 생성
         * 2. OAuthAccount에 Member 연결
         */
        Member member;
        if(memberService.notExistsByOauthAccount(oauthAccount)) {
            log.info("OAuth2SuccessHandler: 신규 회원 가입 - 이메일: {}", oauthAccount.getEmail());
            member = memberService.createMember(oauthAccount);
            oAuthAccountService.linkMember(oauthAccount, member); // OAuthAccount → Member FK 연결
        }else{
            log.info("OAuth2SuccessHandler: 기존 회원 로그인 - 이메일: {}", oauthAccount.getEmail());
            member = memberService.getByOauthAccount(oauthAccount);
        }

        // 리프레시 토큰 생성 -> Redis 세션에 저장
        String refreshToken = tokenProvider.generateToken(member, jwtProperties.refreshTokenExpiry());
        saveRefreshToken(member, refreshToken, request);
        // 액세스 토큰 생성
        String accessToken = tokenProvider.generateToken(member, jwtProperties.accessTokenExpiry());

        // 토큰을 HttpOnly 쿠키로 전달 (URL 쿼리파라미터 노출 방지)
        addTokenCookie(response, ACCESS_TOKEN_COOKIE_NAME, accessToken, jwtProperties.refreshTokenExpiry());
        addTokenCookie(response, REFRESH_TOKEN_COOKIE_NAME, refreshToken, jwtProperties.refreshTokenExpiry());

        // 인증 관련 설정값과 쿠키 제거
        clearAuthenticationAttributes(request, response);

        // 토큰 없이 콜백 경로로만 리다이렉트
        getRedirectStrategy().sendRedirect(request, response, REDIRECT_PATH);
    }

    // 생성된 리프레시 토큰을 세션 메타데이터(ip, deviceInfo)와 함께 Redis에 저장
    private void saveRefreshToken(Member member, String newRefreshToken, HttpServletRequest request) {
        String ip = HttpRequestUtils.extractClientIp(request);
        String deviceInfo = request.getHeader("User-Agent");
        refreshTokenSessionService.save(member, newRefreshToken, ip, deviceInfo);
    }

    // 토큰을 HttpOnly 쿠키로 응답에 추가. domain/secure/sameSite 는 환경별 CookieProperties 로 분기.
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

    // 인증 관련 설정값과 쿠키 제거
    // 인증 프로세스를 진행하면서 세션과 쿠키에 임시로 저장해둔 인증 관련 데이터를 제거한다
    private void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        authorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }

}
