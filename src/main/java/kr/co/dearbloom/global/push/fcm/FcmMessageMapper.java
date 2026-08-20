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
 * <p>플랫폼별 블록({@code apns} / {@code android})을 함께 실어 보낸다. FCM 이 대상 토큰의 플랫폼에
 * 맞는 블록만 골라 쓰므로, 한 요청으로 iOS·Android 양쪽을 커버한다.
 */
@Component
public class FcmMessageMapper {
    /**
     * Android 알림 채널 ID. <b>앱의 채널 생성 코드(nativePush.ts 의 ANDROID_CHANNEL_ID)와 반드시 같아야 한다</b> —
     * 어긋나면 알림이 에러 없이 누락되고 발송 로그는 SUCCESS 로 남아 원인을 찾기 어렵다.
     */
    static final String NOTIFICATION_CHANNEL_ID = "dearbloom-default";

    public Map<String, Object> toRequestBody(String deviceToken, PushMessage message) {
        Map<String, Object> fcmMessage = new LinkedHashMap<>();
        fcmMessage.put("token", deviceToken);
        fcmMessage.put("notification", Map.of("title", message.title(), "body", message.body()));
        fcmMessage.put("data", message.data());
        fcmMessage.put("apns", apnsConfig(message));
        fcmMessage.put("android", androidConfig());

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

    /**
     * Android 블록.
     *
     * <ul>
     *   <li>{@code priority: high} — 잠금화면에 즉시 띄운다. 기본값은 지연 전송될 수 있다</li>
     *   <li>{@code default_sound} — 없으면 무음으로 도착한다</li>
     * </ul>
     *
     * <p>{@code channel_id} — Android 8+ 는 채널이 있어야 알림을 표시한다. 앱이 시작할 때
     * notifee 로 같은 ID 의 채널("디어블룸 알림")을 만들어 두므로 사용자 알림 설정에도 그 이름으로 노출된다.
     *
     * <p>제목·본문은 최상위 {@code notification} 블록을 그대로 쓰므로 여기서 다시 넣지 않는다.
     */
    private Map<String, Object> androidConfig() {
        return Map.of(
                "priority", "high",
                "notification", Map.of(
                        "channel_id", NOTIFICATION_CHANNEL_ID,
                        "default_sound", true));
    }
}
