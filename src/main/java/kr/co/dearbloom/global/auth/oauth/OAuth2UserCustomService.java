package kr.co.dearbloom.global.auth.oauth;

import kr.co.dearbloom.domain.auth.entity.OAuthAccount;
import kr.co.dearbloom.domain.auth.service.OAuthAccountService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class OAuth2UserCustomService extends DefaultOAuth2UserService {
    private final OAuthAccountService oAuthAccountService;

    // 소셜 계정과 앱 연결 로그인 최초에 한번 시행
    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 요청을 바탕으로 사용자 정보를 담은 객체 반환(load)
        // 사용자 객체는 식별자, 이름, 이메일, 프로필 사진 링크 등의 정보를 담고 있다.
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId();
        OAuthAccount oauthAccount = oAuthAccountService.createOrUpdate(oAuth2User, provider);

        // CustomOAuth2User로 래핑해서 반환
        return new CustomOAuth2User(oAuth2User,oauthAccount);
    }

}
