package kr.co.dearbloom.domain.auth.service;

import kr.co.dearbloom.domain.auth.dto.SocialUserInfo;
import kr.co.dearbloom.domain.auth.entity.OAuthAccount;
import kr.co.dearbloom.domain.auth.entity.OAuthProvider;
import kr.co.dearbloom.domain.auth.repository.OAuthAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * 소셜 계정 식별 검증. 계정은 provider 가 아니라 <b>oauthId(=sub)</b> 로만 조회하므로,
 * 같은 사람(같은 sub)이 네이티브·웹 어느 경로로 들어와도 같은 OAuthAccount 로 수렴한다.
 */
@ExtendWith(MockitoExtension.class)
class OAuthAccountServiceTest {
    @Mock OAuthAccountRepository oAuthAccountRepository;
    @InjectMocks OAuthAccountService oAuthAccountService;

    private OAuthAccount account(OAuthProvider provider, String sub) {
        return OAuthAccount.builder()
                .oauthProvider(provider).oauthId(sub)
                .email("user@example.com").name("user").build();
    }

    @Test
    void 기존_sub면_기존계정을_반환하고_저장하지_않는다() {
        OAuthAccount existing = account(OAuthProvider.APPLE, "apple-sub");
        given(oAuthAccountRepository.findByOauthId("apple-sub")).willReturn(Optional.of(existing));

        SocialUserInfo userInfo = new SocialUserInfo("apple-sub", "user@example.com", "user");
        OAuthAccount result = oAuthAccountService.findOrCreateNativeAccount(OAuthProvider.APPLE, userInfo);

        assertThat(result).isSameAs(existing);
        verify(oAuthAccountRepository, never()).save(any());
    }

    @Test
    void 신규_sub면_새_계정을_저장한다() {
        given(oAuthAccountRepository.findByOauthId("new-sub")).willReturn(Optional.empty());
        given(oAuthAccountRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        SocialUserInfo userInfo = new SocialUserInfo("new-sub", "user@example.com", "user");
        OAuthAccount result = oAuthAccountService.findOrCreateNativeAccount(OAuthProvider.GOOGLE, userInfo);

        assertThat(result.getOauthId()).isEqualTo("new-sub");
        assertThat(result.getOauthProvider()).isEqualTo(OAuthProvider.GOOGLE);
        verify(oAuthAccountRepository).save(any());
    }

    /** 애플: 앱(네이티브)으로 먼저 가입 → 웹으로 로그인. aud 는 달라도 sub 는 팀 단위라 동일 → 같은 계정. */
    @Test
    void 애플_같은sub는_네이티브_이후_웹로그인해도_같은계정으로_수렴한다() {
        OAuthAccount saved = account(OAuthProvider.APPLE, "apple-team-sub");
        given(oAuthAccountRepository.findByOauthId("apple-team-sub"))
                .willReturn(Optional.empty(), Optional.of(saved));
        given(oAuthAccountRepository.save(any())).willReturn(saved);

        SocialUserInfo fromNative = new SocialUserInfo("apple-team-sub", "user@example.com", "user");
        SocialUserInfo fromWeb = new SocialUserInfo("apple-team-sub", "user@example.com", "user");

        OAuthAccount nativeAccount = oAuthAccountService.findOrCreateNativeAccount(OAuthProvider.APPLE, fromNative);
        OAuthAccount webAccount = oAuthAccountService.findOrCreateNativeAccount(OAuthProvider.APPLE, fromWeb);

        assertThat(webAccount).isSameAs(nativeAccount);
        verify(oAuthAccountRepository, times(1)).save(any());
    }

    /** 구글: 웹(Spring Security createOrUpdate)도 동일한 sub 로 조회 → 네이티브가 만든 계정을 찾는다. */
    @Test
    void 구글_웹로그인은_같은sub의_기존계정을_찾는다() {
        OAuthAccount existing = account(OAuthProvider.GOOGLE, "google-sub");
        given(oAuthAccountRepository.findByOauthId("google-sub")).willReturn(Optional.of(existing));

        OAuth2User oAuth2User = mock(OAuth2User.class);
        given(oAuth2User.getAttributes()).willReturn(Map.of(
                "sub", "google-sub", "email", "user@example.com", "name", "user"));

        OAuthAccount result = oAuthAccountService.createOrUpdate(oAuth2User, "google");

        assertThat(result).isSameAs(existing);
        verify(oAuthAccountRepository, never()).save(any());
    }
}
