package kr.co.dearbloom.global.auth.oauth;

import kr.co.dearbloom.domain.auth.service.AuthService;
import kr.co.dearbloom.domain.auth.service.OAuthOneTimeCodeService;
import kr.co.dearbloom.domain.auth.entity.OAuthAccount;
import kr.co.dearbloom.domain.member.entity.Member;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.co.dearbloom.global.auth.oauth.custom.OAuthLocalEntryController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Slf4j
@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler { // 인증 성공시 실행할 핸들러
    @Value("${url.oauth-callback}")
    private String REDIRECT_PATH;

    private final AuthService authService;
    private final OAuth2AuthorizationRequestBasedOnCookieRepository authorizationRequestRepository;
    private final OAuthOneTimeCodeService oneTimeCodeService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        // OAuth2UserCustomService의 loadUser()에서 반환한 OAuth2User 사용
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // CustomOAuth2User로 캐스팅
        if (!(oAuth2User instanceof CustomOAuth2User)) {
            throw new IllegalArgumentException("OAuth2User is not an instance of CustomOAuth2User");
        }
        CustomOAuth2User customUser = (CustomOAuth2User) oAuth2User;
        OAuthAccount oauthAccount = customUser.getOauthAccount();

        // 회원 조회/생성(신규 가입시 Member 생성 + OAuthAccount 연결) — AuthService 공용 로직
        Member member = authService.findOrCreateMemberByOAuthAccount(oauthAccount);

        // 하이브리드 분기: 로컬 웹이 개발 서버로 로그인한 경우(진입점에서 표식 쿠키를 심어둠).
        // localhost 는 백엔드 Set-Cookie 를 못 받으므로, 토큰 대신 1회용 oneTimeCode 만 넘겨
        // (Google 자체 authorization code 와 구분하기 위해 "code"가 아닌 "oneTimeCode"로 명명)
        // 로컬 Next.js 가 oneTimeCode→토큰 교환 후 자기 도메인 쿠키를 심게 한다.
        String localTarget = readLocalTargetCookie(request);
        if (localTarget != null) {
            String oneTimeCode = oneTimeCodeService.issue(member.getMemberId());
            deleteLocalTargetCookie(response);
            clearAuthenticationAttributes(request, response);
            String redirectUrl = UriComponentsBuilder.fromUriString(localTarget)
                    .queryParam("oneTimeCode", oneTimeCode)
                    .build().toUriString();
            getRedirectStrategy().sendRedirect(request, response, redirectUrl);
            return;
        }

        // 일반 분기: dev 웹 / 운영 — 기존대로 백엔드가 직접 쿠키를 심는다.
        authService.issueTokensAndSetCookies(member, request, response);
        clearAuthenticationAttributes(request, response);
        getRedirectStrategy().sendRedirect(request, response, REDIRECT_PATH);
    }

    /** 진입점(OAuthLocalEntryController)이 심은 표식 쿠키에서 로컬 콜백 URL 을 읽는다. 없으면 null. */
    private String readLocalTargetCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        return Arrays.stream(request.getCookies())
                .filter(c -> OAuthLocalEntryController.LOCAL_TARGET_COOKIE.equals(c.getName()))
                .map(c -> URLDecoder.decode(c.getValue(), StandardCharsets.UTF_8))
                .findFirst()
                .orElse(null);
    }

    private void deleteLocalTargetCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(OAuthLocalEntryController.LOCAL_TARGET_COOKIE, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    // 인증 관련 설정값과 쿠키 제거
    // 인증 프로세스를 진행하면서 세션과 쿠키에 임시로 저장해둔 인증 관련 데이터를 제거한다
    private void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        authorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }

}
