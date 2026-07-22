package kr.co.dearbloom.global.auth.oauth.custom;

import jakarta.servlet.http.Cookie;
import kr.co.dearbloom.domain.auth.dto.SocialUserInfo;
import kr.co.dearbloom.domain.auth.entity.OAuthAccount;
import kr.co.dearbloom.domain.auth.entity.OAuthProvider;
import kr.co.dearbloom.domain.auth.service.custom.AppleNativeAuthService;
import kr.co.dearbloom.domain.auth.service.AuthService;
import kr.co.dearbloom.domain.auth.service.OAuthAccountService;
import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.domain.member.entity.MemberRole;
import kr.co.dearbloom.global.dto.response.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * 애플 웹 로그인 서비스 검증.
 * - authorize: 애플 인증 URL 조립 + state 쿠키(SameSite=None; Secure)
 * - callback: error / state 불일치 / 정상 분기
 */
@ExtendWith(MockitoExtension.class)
class AppleWebLoginServiceTest {
    private static final String CLIENT_ID = "com.dearbloom.service.test";
    private static final String REDIRECT_URI = "https://api.test/oauth2/apple/callback";
    private static final String FRONTEND_CALLBACK = "https://web.test/api/auth/callback";
    private static final String STATE_COOKIE = "apple_oauth_state";

    @Mock AppleNativeAuthService appleNativeAuthService;
    @Mock OAuthAccountService oAuthAccountService;
    @Mock AuthService authService;

    private AppleWebLoginService service;

    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    @BeforeEach
    void setUp() {
        service = new AppleWebLoginService(appleNativeAuthService, oAuthAccountService, authService);
        ReflectionTestUtils.setField(service, "webClientId", CLIENT_ID);
        ReflectionTestUtils.setField(service, "redirectUri", REDIRECT_URI);
        ReflectionTestUtils.setField(service, "frontendCallback", FRONTEND_CALLBACK);
    }

    @Test
    void authorize_애플_URL과_state쿠키를_만든다() {
        String url = service.createAuthorizeUrl(MemberRole.CUSTOMER, response);

        assertThat(url)
                .startsWith("https://appleid.apple.com/auth/authorize")
                .contains("client_id=" + CLIENT_ID)
                .contains("response_mode=form_post")
                .contains("scope=")
                .contains("state=");

        String setCookie = response.getHeader("Set-Cookie");
        assertThat(setCookie)
                .contains(STATE_COOKIE + "=")
                .contains("SameSite=None")
                .contains("Secure")
                .contains("HttpOnly");
    }

    @Test
    void callback_error가_오면_실패파라미터를_붙여_프론트로_보낸다() {
        String redirect = service.handleCallback(null, null, "user_cancelled_authorize", request, response);

        assertThat(redirect).isEqualTo(FRONTEND_CALLBACK + "?error=apple_login_failed");
        verifyNoInteractions(appleNativeAuthService);
    }

    @Test
    void callback_state가_불일치하면_예외를_던진다() {
        request.setCookies(new Cookie(STATE_COOKIE, "server-state"));

        assertThatThrownBy(() ->
                service.handleCallback("id-token", "attacker-state", null, request, response))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("code", "PARAMETER-400");
        verifyNoInteractions(appleNativeAuthService);
    }

    @Test
    void callback_정상이면_검증_회원처리_쿠키발급_후_프론트로_보낸다() {
        request.setCookies(new Cookie(STATE_COOKIE, "state-123"));
        SocialUserInfo userInfo = new SocialUserInfo("apple-sub", "user@example.com", "user");
        OAuthAccount acc = OAuthAccount.builder()
                .oauthProvider(OAuthProvider.APPLE).oauthId("apple-sub")
                .email("user@example.com").name("user").build();
        Member member = Mockito.mock(Member.class);

        given(appleNativeAuthService.verifyIdentityToken("id-token")).willReturn(userInfo);
        given(oAuthAccountService.findOrCreateNativeAccount(OAuthProvider.APPLE, userInfo)).willReturn(acc);
        given(authService.findOrCreateMemberByOAuthAccount(acc)).willReturn(member);

        String redirect = service.handleCallback("id-token", "state-123", null, request, response);

        assertThat(redirect).isEqualTo(FRONTEND_CALLBACK);
        verify(appleNativeAuthService).verifyIdentityToken("id-token");
        // signup_role 쿠키가 없으므로 selectedRole=null → override 도 null
        verify(authService).issueTokensAndSetCookies(member, null, request, response);
    }
}
