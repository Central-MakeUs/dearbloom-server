package kr.co.dearbloom.domain.auth.facade;

import kr.co.dearbloom.domain.auth.dto.NativeLoginRequest;
import kr.co.dearbloom.domain.auth.dto.SocialUserInfo;
import kr.co.dearbloom.domain.auth.entity.OAuthAccount;
import kr.co.dearbloom.domain.auth.entity.OAuthProvider;
import kr.co.dearbloom.domain.auth.service.custom.AppleNativeAuthService;
import kr.co.dearbloom.domain.auth.service.AuthService;
import kr.co.dearbloom.domain.auth.service.custom.GoogleNativeAuthService;
import kr.co.dearbloom.domain.auth.service.OAuthAccountService;
import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.domain.member.entity.MemberRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * 네이티브 소셜 로그인 provider 라우팅 검증.
 * - 구글 → GoogleNativeAuthService, 애플 → AppleNativeAuthService 로 분기
 */
@ExtendWith(MockitoExtension.class)
class AuthFacadeTest {
    @Mock OAuthAccountService oAuthAccountService;
    @Mock GoogleNativeAuthService googleNativeAuthService;
    @Mock AppleNativeAuthService appleNativeAuthService;
    @Mock AuthService authService;
    @InjectMocks AuthFacade authFacade;

    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    private NativeLoginRequest nativeRequest(OAuthProvider provider, String token) {
        NativeLoginRequest req = new NativeLoginRequest();
        ReflectionTestUtils.setField(req, "socialProvider", provider);
        ReflectionTestUtils.setField(req, "socialToken", token);
        ReflectionTestUtils.setField(req, "role", MemberRole.CUSTOMER);
        return req;
    }

    private OAuthAccount account(OAuthProvider provider, String sub) {
        return OAuthAccount.builder()
                .oauthProvider(provider)
                .oauthId(sub)
                .email("user@example.com")
                .name("user")
                .build();
    }

    @Test
    void 구글_네이티브_로그인은_구글서비스로_분기한다() {
        SocialUserInfo userInfo = new SocialUserInfo("g-sub", "user@example.com", "user");
        OAuthAccount acc = account(OAuthProvider.GOOGLE, "g-sub");
        Member member = Mockito.mock(Member.class);

        given(googleNativeAuthService.exchangeServerAuthCode("server-auth-code")).willReturn(userInfo);
        given(oAuthAccountService.findOrCreateNativeAccount(OAuthProvider.GOOGLE, userInfo)).willReturn(acc);
        given(authService.findOrCreateMemberByOAuthAccount(acc)).willReturn(member);

        authFacade.nativeLogin(nativeRequest(OAuthProvider.GOOGLE, "server-auth-code"), request, response);

        verify(googleNativeAuthService).exchangeServerAuthCode("server-auth-code");
        verify(authService).issueTokensAndSetCookies(member, null, request, response);
        verifyNoInteractions(appleNativeAuthService);
    }

    @Test
    void 애플_네이티브_로그인은_애플서비스로_분기한다() {
        SocialUserInfo userInfo = new SocialUserInfo("a-sub", "user@example.com", "user");
        OAuthAccount acc = account(OAuthProvider.APPLE, "a-sub");
        Member member = Mockito.mock(Member.class);

        given(appleNativeAuthService.verifyIdentityToken("identity-token")).willReturn(userInfo);
        given(oAuthAccountService.findOrCreateNativeAccount(OAuthProvider.APPLE, userInfo)).willReturn(acc);
        given(authService.findOrCreateMemberByOAuthAccount(acc)).willReturn(member);

        authFacade.nativeLogin(nativeRequest(OAuthProvider.APPLE, "identity-token"), request, response);

        verify(appleNativeAuthService).verifyIdentityToken("identity-token");
        verify(authService).issueTokensAndSetCookies(member, null, request, response);
        verifyNoInteractions(googleNativeAuthService);
    }
}
