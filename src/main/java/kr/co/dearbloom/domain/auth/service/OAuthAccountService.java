package kr.co.dearbloom.domain.auth.service;

import kr.co.dearbloom.domain.auth.dto.SocialUserInfo;
import kr.co.dearbloom.domain.auth.entity.OAuthAccount;
import kr.co.dearbloom.domain.auth.entity.OAuthProvider;
import kr.co.dearbloom.domain.auth.repository.OAuthAccountRepository;
import kr.co.dearbloom.domain.auth.service.custom.AppleTokenService;
import kr.co.dearbloom.domain.member.entity.Member;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthAccountService {
    private final OAuthAccountRepository oAuthAccountRepository;
    private final AppleTokenService appleTokenService;

    @Transactional
    public OAuthAccount createOrUpdate(OAuth2User oAuth2User, String provider) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email, name, oauthId;
        OAuthProvider oauthProvider;
        Optional<OAuthAccount> existingAccount;
        switch (provider) {
            case "google":
                oauthId = (String) attributes.get("sub");
                existingAccount = oAuthAccountRepository.findByOauthId(oauthId);
                if(existingAccount.isPresent()) return existingAccount.get();

                email = (String) attributes.get("email");
                name = (String) attributes.get("name");
                oauthProvider = OAuthProvider.GOOGLE;
                break;
            // NAVER/KAKAO 미지원 (OAuthProvider enum 에서 주석 처리됨). 지원 재개 시 enum 과 함께 복구.
//            case "kakao":
//                oauthId = attributes.get("id").toString();
//                existingAccount = oAuthAccountRepository.findByOauthId(oauthId);
//                if(existingAccount.isPresent()) return existingAccount.get();
//
//                Map attributesProperties = (Map) attributes.get("properties");
//                name = (String) attributesProperties.get("nickname");
//                Map attributesKakaoAcount = (Map) attributes.get("kakao_account");
//                email = (String) attributesKakaoAcount.get("email");
//                oauthProvider = OAuthProvider.KAKAO;
//                break;
//            case "naver": // ToDo: 네이버 파라미터 확인 필요
//                Map attributesResponse = (Map) attributes.get("response");
//                oauthId = attributesResponse.get("id").toString();
//                existingAccount = oAuthAccountRepository.findByOauthId(oauthId);
//                if(existingAccount.isPresent()) return existingAccount.get();
//
//                name = (String) attributesResponse.get("name");
//                email = (String) attributesResponse.get("email");
//                oauthProvider = OAuthProvider.NAVER;
//                break;
            default:
                throw new IllegalArgumentException("Unknown provider: " + provider);
        }
        return oAuthAccountRepository.save(OAuthAccount.builder()
                .oauthProvider(oauthProvider)
                .oauthId(oauthId)
                .email(email)
                .name(name)
                .build());
    }

    @Transactional
    public OAuthAccount createSampleAccount(String nickname, String email) {
        return oAuthAccountRepository.save(OAuthAccount.builder()
                .oauthProvider(OAuthProvider.GOOGLE)
                .email(email)
                .oauthId("oauth_"+nickname)
                .name(nickname)
                .build());
    }

    /** OAuthAccount(FK 소유측)에 Member 연결 후 영속화. */
    @Transactional
    public void linkMember(OAuthAccount oauthAccount, Member member) {
        oauthAccount.linkMember(member);
        oAuthAccountRepository.save(oauthAccount); // detached 면 merge 로 FK 반영
    }

    @Transactional
    public void deleteByMember(Member member) {
        oAuthAccountRepository.findByMember(member)
                .ifPresent(oAuthAccountRepository::delete);
    }

    /** Apple 로그인 code 교환으로 얻은 refresh token 을 계정에 저장(탈퇴 revoke 용). */
    @Transactional
    public void updateRefreshToken(OAuthAccount account, String refreshToken, String clientId) {
        account.updateRefreshToken(refreshToken, clientId);
        oAuthAccountRepository.save(account);
    }

    /**
     * 탈퇴 시 이 회원의 Apple refresh token 을 폐기(App Store 필수).
     * Apple 계정 + refresh token 이 있을 때만 호출. revoke 실패는 호출부에서 무시(탈퇴는 진행).
     */
    public void revokeAppleTokenIfPresent(Member member) {
        oAuthAccountRepository.findByMember(member).ifPresent(account -> {
            if (account.getOauthProvider() == OAuthProvider.APPLE
                    && account.getOauthRefreshToken() != null
                    && account.getOauthRefreshClientId() != null) {
                appleTokenService.revoke(account.getOauthRefreshToken(), account.getOauthRefreshClientId());
            }
        });
    }

    public boolean existsByName(String name) {
        return oAuthAccountRepository.existsByName(name);
    }

    /** 네이티브 SDK 로그인용 — oauthId 로 계정을 조회하고 없으면 새로 만든다. */
    @Transactional
    public OAuthAccount findOrCreateNativeAccount(OAuthProvider provider, SocialUserInfo userInfo) {
        return oAuthAccountRepository.findByOauthId(userInfo.sub())
                .orElseGet(() -> oAuthAccountRepository.save(OAuthAccount.builder()
                        .oauthProvider(provider)
                        .oauthId(userInfo.sub())
                        .email(userInfo.email())
                        .name(userInfo.name())
                        .build()));
    }
}
