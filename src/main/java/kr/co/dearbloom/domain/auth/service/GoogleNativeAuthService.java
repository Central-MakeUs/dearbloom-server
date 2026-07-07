package kr.co.dearbloom.domain.auth.service;

import kr.co.dearbloom.global.dto.response.exception.CustomException;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/** 네이티브 앱(WebView)에서 획득한 Google serverAuthCode 를 서버에서 토큰으로 교환. */
@Slf4j
@Service
public class GoogleNativeAuthService {
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    private static final String TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public GoogleNativeAuthService(RestClient.Builder restClientBuilder, ObjectMapper objectMapper) {
        this.restClient = restClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public record GoogleUserInfo(String sub, String email, String name) {}

    public GoogleUserInfo exchangeServerAuthCode(String serverAuthCode) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("code", serverAuthCode);
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("redirect_uri", "");
        form.add("grant_type", "authorization_code");

        Map<String, Object> tokenResponse;
        try {
            tokenResponse = restClient.post()
                    .uri(TOKEN_ENDPOINT)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        log.warn("[GoogleNativeAuth] token exchange failed — status: {}", res.getStatusCode());
                        throw new CustomException(ErrorCode.INVALID_OAUTH_TOKEN);
                    })
                    .body(new ParameterizedTypeReference<>() {});
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.warn("[GoogleNativeAuth] token exchange error: {}", e.getMessage());
            throw new CustomException(ErrorCode.INVALID_OAUTH_TOKEN);
        }

        if (tokenResponse == null || !tokenResponse.containsKey("id_token")) {
            throw new CustomException(ErrorCode.INVALID_OAUTH_TOKEN);
        }

        return parseIdToken((String) tokenResponse.get("id_token"));
    }

    private GoogleUserInfo parseIdToken(String idToken) {
        try {
            String[] parts = idToken.split("\\.");
            if (parts.length < 2) throw new CustomException(ErrorCode.INVALID_OAUTH_TOKEN);

            byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
            @SuppressWarnings("unchecked")
            Map<String, Object> claims = objectMapper.readValue(
                    new String(decoded, StandardCharsets.UTF_8), Map.class);

            String sub = (String) claims.get("sub");
            String email = (String) claims.get("email");
            String name = (String) claims.getOrDefault("name", email);

            if (sub == null || email == null) throw new CustomException(ErrorCode.INVALID_OAUTH_TOKEN);

            return new GoogleUserInfo(sub, email, name);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.warn("[GoogleNativeAuth] id_token parse error: {}", e.getMessage());
            throw new CustomException(ErrorCode.INVALID_OAUTH_TOKEN);
        }
    }
}
