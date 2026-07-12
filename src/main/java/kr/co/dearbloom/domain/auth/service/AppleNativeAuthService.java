package kr.co.dearbloom.domain.auth.service;

import kr.co.dearbloom.domain.auth.dto.SocialUserInfo;
import kr.co.dearbloom.global.dto.response.exception.CustomException;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 네이티브 앱(iOS) / 웹에서 Sign in with Apple 로 얻은 identityToken(JWT)을 검증한다.
 * Google 은 serverAuthCode 를 교환하지만, Apple 은 앱/웹이 이미 받은 identityToken 을 그대로 받아
 * Apple 공개키(JWKS)로 서명·발급자(iss)·대상(aud)을 검증한 뒤 sub/email 을 추출한다.
 * (client_secret(ES256) 을 직접 생성하는 토큰 교환 방식이 아니라, 이미 서명된 JWT 를 신뢰검증만 함)
 */
@Slf4j
@Service
public class AppleNativeAuthService {
    private static final String ISSUER = "https://appleid.apple.com";
    private static final String JWK_SET_URI = "https://appleid.apple.com/auth/keys";

    private final JwtDecoder jwtDecoder;

    public AppleNativeAuthService(@Value("${apple.client-ids}") String clientIds) {
        List<String> allowedAudiences = Arrays.stream(clientIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(JWK_SET_URI).build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(),
                new JwtIssuerValidator(ISSUER),
                audienceValidator(allowedAudiences)
        ));
        this.jwtDecoder = decoder;
    }

    public SocialUserInfo verifyIdentityToken(String identityToken) {
        try {
            Jwt jwt = jwtDecoder.decode(identityToken);

            String sub = jwt.getSubject();
            if (sub == null) throw new CustomException(ErrorCode.INVALID_OAUTH_TOKEN);

            // Apple 은 이메일 공유에 동의한 첫 로그인에만 email 을 넣어준다. 이후 로그인은 sub 로 계정 식별.
            String email = jwt.getClaimAsString("email");
            String resolvedEmail = (email != null) ? email : sub + "@privaterelay.appleid.com";

            return new SocialUserInfo(sub, resolvedEmail, resolvedEmail);
        } catch (JwtException e) {
            log.warn("[AppleNativeAuth] identityToken 검증 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.INVALID_OAUTH_TOKEN);
        }
    }

    private OAuth2TokenValidator<Jwt> audienceValidator(List<String> allowedAudiences) {
        return jwt -> {
            List<String> aud = jwt.getAudience();
            if (aud != null && aud.stream().anyMatch(allowedAudiences::contains)) {
                return OAuth2TokenValidatorResult.success();
            }
            log.warn("[AppleNativeAuth] aud 불일치 — token aud: {}, 허용: {}", aud, allowedAudiences);
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "Apple identityToken aud 불일치", null));
        };
    }
}
