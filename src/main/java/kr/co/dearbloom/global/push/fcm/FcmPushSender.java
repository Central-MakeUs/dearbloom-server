package kr.co.dearbloom.global.push.fcm;

import kr.co.dearbloom.domain.notification.message.PushMessage;
import kr.co.dearbloom.global.push.PushSendResult;
import kr.co.dearbloom.global.push.PushSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.Optional;

/**
 * FCM HTTP v1 발송 어댑터.
 *
 * <p>Firebase Admin SDK 없이 직접 호출한다. access token 발급·캐싱은
 * {@link GoogleAccessTokenProvider} 가, 페이로드 변환은 {@link FcmMessageMapper} 가 맡는다.
 *
 * <p><b>예외를 던지지 않는다.</b> 푸시 실패가 비즈니스 흐름을 건드리면 안 되므로 결과 enum 으로만 답한다.
 * 에러 응답도 그대로 읽어야 죽은 토큰을 가려낼 수 있어서, 상태코드로 예외를 던지는 {@code retrieve()} 대신
 * {@code exchange()} 로 응답을 직접 다룬다.
 */
@Slf4j
@Component
public class FcmPushSender implements PushSender {
    private static final String SEND_ENDPOINT = "https://fcm.googleapis.com/v1/projects/%s/messages:send";
    private static final ParameterizedTypeReference<Map<String, Object>> JSON_MAP =
            new ParameterizedTypeReference<>() {};

    private final FcmProperties properties;
    private final GoogleAccessTokenProvider accessTokenProvider;
    private final FcmMessageMapper messageMapper;
    private final RestClient restClient;

    public FcmPushSender(
            FcmProperties properties,
            GoogleAccessTokenProvider accessTokenProvider,
            FcmMessageMapper messageMapper,
            RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.accessTokenProvider = accessTokenProvider;
        this.messageMapper = messageMapper;
        this.restClient = restClientBuilder.build();
    }

    @Override
    public PushSendResult send(String deviceToken, PushMessage message) {
        if (!properties.enabled() || !properties.isConfigured()) {
            log.info("[FCM] 비활성 상태라 발송을 건너뜁니다 — title={}, deepLink={}",
                    message.title(), message.deepLink());
            return PushSendResult.FAILURE;
        }

        Optional<String> accessToken = accessTokenProvider.accessToken();
        if (accessToken.isEmpty()) {
            return PushSendResult.RETRYABLE_FAILURE;
        }

        try {
            return restClient.post()
                    .uri(SEND_ENDPOINT.formatted(properties.projectId()))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.get())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(messageMapper.toRequestBody(deviceToken, message))
                    .exchange((request, response) -> {
                        HttpStatusCode status = response.getStatusCode();
                        if (status.is2xxSuccessful()) {
                            return PushSendResult.SUCCESS;
                        }
                        return classify(status, readErrorStatus(response.bodyTo(JSON_MAP)));
                    });
        } catch (Exception e) {
            log.warn("[FCM] 발송 중 오류: {}", e.getMessage());
            return PushSendResult.RETRYABLE_FAILURE;
        }
    }

    /**
     * FCM 에러를 삭제 대상 / 재시도 대상으로 나눈다.
     *
     * <ul>
     *   <li>{@code UNREGISTERED}(404) — 앱 삭제·재설치로 토큰이 죽음 → 즉시 삭제</li>
     *   <li>{@code INVALID_ARGUMENT}(400) — 토큰 형식 자체가 잘못됨 → 즉시 삭제</li>
     *   <li>429 / 5xx — 일시 오류 → 토큰 유지</li>
     * </ul>
     */
    private PushSendResult classify(HttpStatusCode httpStatus, String errorStatus) {
        if ("UNREGISTERED".equals(errorStatus) || "INVALID_ARGUMENT".equals(errorStatus)) {
            log.info("[FCM] 무효 토큰 — status={} → 삭제", errorStatus);
            return PushSendResult.TOKEN_INVALID;
        }
        if (httpStatus.is5xxServerError() || httpStatus.value() == 429) {
            log.warn("[FCM] 일시 오류 — http={}, status={}", httpStatus, errorStatus);
            return PushSendResult.RETRYABLE_FAILURE;
        }
        log.warn("[FCM] 발송 실패 — http={}, status={}", httpStatus, errorStatus);

        return PushSendResult.FAILURE;
    }

    /** 에러 본문 {@code {"error": {"status": "UNREGISTERED", ...}}} 에서 status 만 꺼낸다. */
    @SuppressWarnings("unchecked")
    private String readErrorStatus(Map<String, Object> body) {
        if (body == null || !(body.get("error") instanceof Map<?, ?> error)) {
            return null;
        }
        return ((Map<String, Object>) error).get("status") instanceof String status ? status : null;
    }
}
