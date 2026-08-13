package kr.co.dearbloom.global.push.fcm;

import kr.co.dearbloom.domain.notification.message.PushMessage;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@link PushMessage} 를 FCM HTTP v1 요청 본문으로 변환한다.
 *
 * <p><b>notification + data 를 함께 보낸다.</b> notification 블록이 있으면 앱이 백그라운드일 때
 * OS 가 알아서 배너를 띄우고, 포그라운드에서는 앱이 data 를 읽어 직접 처리한다. 딥링크는 어느 경우든
 * data 로 전달돼야 해서 둘 다 채운다.
 *
 * <p>1차 범위가 iOS 뿐이라 {@code apns} 블록만 실전에서 검증된다. {@code android} 블록은
 * Android 를 켤 때 알림 채널·priority 를 다시 확인해야 한다.
 */
@Component
public class FcmMessageMapper {
    public Map<String, Object> toRequestBody(String deviceToken, PushMessage message) {
        Map<String, Object> fcmMessage = new LinkedHashMap<>();
        fcmMessage.put("token", deviceToken);
        fcmMessage.put("notification", Map.of("title", message.title(), "body", message.body()));
        fcmMessage.put("data", message.data());
        fcmMessage.put("apns", apnsConfig(message));

        return Map.of("message", fcmMessage);
    }

    /**
     * APNs 블록.
     * <ul>
     *   <li>{@code apns-priority: 10} — 사용자에게 즉시 보여줄 알림</li>
     *   <li>{@code sound: default} — 없으면 무음으로 조용히 도착한다</li>
     *   <li>{@code content-available} 은 넣지 않는다 — 무음 백그라운드 갱신용이라 이번 요건과 무관하고,
     *       넣으면 iOS 가 전송을 조절(throttle)할 수 있다</li>
     * </ul>
     */
    private Map<String, Object> apnsConfig(PushMessage message) {
        Map<String, Object> aps = Map.of(
                "alert", Map.of("title", message.title(), "body", message.body()),
                "sound", "default");

        return Map.of(
                "headers", Map.of("apns-priority", "10"),
                "payload", Map.of("aps", aps));
    }
}
