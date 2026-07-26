package kr.co.dearbloom.domain.auth.service.custom;

import kr.co.dearbloom.global.dto.response.exception.CustomException;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Apple 토큰 엔드포인트 연동.
 * - {@link #exchangeAuthorizationCode}: 로그인 때 authorization code 를 refresh token 으로 교환(저장 후 탈퇴 revoke 에 사용).
 * - {@link #revoke}: 탈퇴 시 refresh token 폐기(App Store 심사 필수).
 * client_secret 은 {@link AppleClientSecretGenerator} 로 clientId 별 생성한다.
 */
@Slf4j
@Service
public class AppleTokenService {
    private static final String TOKEN_ENDPOINT = "https://appleid.apple.com/auth/token";
    private static final String REVOKE_ENDPOINT = "https://appleid.apple.com/auth/revoke";

    private final RestClient restClient;
    private final AppleClientSecretGenerator clientSecretGenerator;

    public AppleTokenService(RestClient.Builder restClientBuilder,
                             AppleClientSecretGenerator clientSecretGenerator) {
        this.restClient = restClientBuilder.build();
        this.clientSecretGenerator = clientSecretGenerator;
    }

    /**
     * authorization code → refresh token 교환. redirectUri 는 웹 플로우에서만 필요(네이티브는 null).
     * refresh token 이 없으면 null 반환(로그인은 계속 진행하되 탈퇴 시 revoke 불가).
     */
    public String exchangeAuthorizationCode(String code, String clientId, String redirectUri) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", clientId);
        form.add("client_secret", clientSecretGenerator.generate(clientId));
        form.add("code", code);
        form.add("grant_type", "authorization_code");
        if (redirectUri != null && !redirectUri.isBlank()) {
            form.add("redirect_uri", redirectUri);
        }

        try {
            Map<String, Object> response = restClient.post()
                    .uri(TOKEN_ENDPOINT)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        log.warn("[AppleToken] code 교환 실패 — status: {}", res.getStatusCode());
                        throw new CustomException(ErrorCode.INVALID_OAUTH_TOKEN);
                    })
                    .body(new ParameterizedTypeReference<>() {});
            return response == null ? null : (String) response.get("refresh_token");
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.warn("[AppleToken] code 교환 오류: {}", e.getMessage());
            throw new CustomException(ErrorCode.INVALID_OAUTH_TOKEN);
        }
    }

    /** refresh token 폐기. 실패 시 예외를 던지므로 호출부(탈퇴)에서 try/catch 로 무시 처리한다. */
    public void revoke(String refreshToken, String clientId) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", clientId);
        form.add("client_secret", clientSecretGenerator.generate(clientId));
        form.add("token", refreshToken);
        form.add("token_type_hint", "refresh_token");

        restClient.post()
                .uri(REVOKE_ENDPOINT)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    log.warn("[AppleToken] revoke 실패 — status: {}", res.getStatusCode());
                    throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
                })
                .toBodilessEntity();
    }
}
