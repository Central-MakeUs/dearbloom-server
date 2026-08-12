package kr.co.dearbloom.global.push.fcm;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * FCM HTTP v1 호출에 쓸 Google OAuth2 access token 을 발급·캐싱한다.
 *
 * <p>Firebase Admin SDK 를 쓰지 않는 대신 서비스 계정 흐름을 직접 구현한다:
 * 서비스 계정 개인키로 <b>RS256 JWT assertion</b> 을 만들어 구글 토큰 엔드포인트에 제출하고,
 * 돌아온 access token 을 만료 전까지 재사용한다.
 * (Apple 쪽 {@code AppleClientSecretGenerator} 의 ES256 서명과 같은 패턴이고 알고리즘만 다르다.)
 *
 * <p>토큰 수명은 보통 1시간이다. 시계 오차와 발송 도중 만료를 피하려고 만료 1분 전에 미리 버린다.
 */
@Slf4j
@Component
public class GoogleAccessTokenProvider {
    private static final String TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";
    private static final String SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
    private static final String GRANT_TYPE = "urn:ietf:params:oauth:grant-type:jwt-bearer";
    private static final Duration ASSERTION_LIFETIME = Duration.ofMinutes(60);
    private static final Duration EXPIRY_SKEW = Duration.ofMinutes(1);

    private final FcmProperties properties;
    private final RestClient restClient;

    /** 캐시된 토큰. 발송이 여러 스레드에서 동시에 일어나므로 참조 교체만으로 안전하게 다룬다. */
    private volatile CachedToken cached;

    public GoogleAccessTokenProvider(FcmProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClient = restClientBuilder.build();
    }

    /** 유효한 access token. 발급에 실패하면 비어 있는 값을 준다(호출측이 발송을 건너뛴다). */
    public Optional<String> accessToken() {
        CachedToken current = cached;
        if (current != null && current.isValid()) {
            return Optional.of(current.value());
        }

        synchronized (this) {
            // 대기하는 동안 다른 스레드가 갱신했을 수 있다.
            if (cached != null && cached.isValid()) {
                return Optional.of(cached.value());
            }
            Optional<CachedToken> issued = issue();
            issued.ifPresent(token -> cached = token);
            return issued.map(CachedToken::value);
        }
    }

    private Optional<CachedToken> issue() {
        try {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("grant_type", GRANT_TYPE);
            form.add("assertion", createAssertion());

            Map<String, Object> response = restClient.post()
                    .uri(TOKEN_ENDPOINT)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, res) -> {
                        throw new IllegalStateException("token endpoint status " + res.getStatusCode());
                    })
                    .body(new ParameterizedTypeReference<>() {});

            if (response == null || !(response.get("access_token") instanceof String token)) {
                log.warn("[FCM] access token 응답에 access_token 이 없습니다.");
                return Optional.empty();
            }
            long expiresIn = response.get("expires_in") instanceof Number n ? n.longValue() : 3600L;

            return Optional.of(new CachedToken(
                    token, Instant.now().plusSeconds(expiresIn).minus(EXPIRY_SKEW)));
        } catch (Exception e) {
            log.warn("[FCM] access token 발급 실패: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private String createAssertion() throws Exception {
        Instant now = Instant.now();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(properties.clientEmail())
                .subject(properties.clientEmail())
                .audience(List.of(TOKEN_ENDPOINT))
                .claim("scope", SCOPE)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plus(ASSERTION_LIFETIME)))
                .build();
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claims);
        jwt.sign(new RSASSASigner(parsePrivateKey()));

        return jwt.serialize();
    }

    /**
     * 서비스 계정 JSON 의 {@code private_key} 는 PKCS#8 PEM 이다.
     * 환경변수로 넣으면 개행이 리터럴 {@code \n} 으로 들어오는 경우가 흔해 함께 정리한다.
     */
    private RSAPrivateKey parsePrivateKey() throws Exception {
        String content = properties.privateKey()
                .replace("\\n", "\n")
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] der = Base64.getDecoder().decode(content);

        return (RSAPrivateKey) KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(der));
    }

    private record CachedToken(String value, Instant expiresAt) {
        boolean isValid() {
            return Instant.now().isBefore(expiresAt);
        }
    }
}
