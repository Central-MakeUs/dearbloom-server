package kr.co.dearbloom.global.push.fcm;

import kr.co.dearbloom.domain.notification.message.PushMessage;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * FCM 요청 본문 검증.
 *
 * <p>Android 는 채널 지정이 앱 상태와 어긋나면 알림이 <b>에러 없이 누락될 수 있다</b>. 발송 로그는
 * SUCCESS 로 남아 원인을 찾기 어려우므로, 페이로드 모양을 테스트로 고정한다.
 */
class FcmMessageMapperTest {
    private final FcmMessageMapper mapper = new FcmMessageMapper();
    private final PushMessage message =
            PushMessage.of("새 문의가 도착했어요", "[작품] 8/27 16:30 촬영 문의예요.", "/app/artist/requests/5");

    @SuppressWarnings("unchecked")
    private Map<String, Object> messageBody() {
        return (Map<String, Object>) mapper.toRequestBody("device-token", message).get("message");
    }

    @Test
    void 공통_필드와_플랫폼별_블록을_함께_싣는다() {
        Map<String, Object> body = messageBody();

        assertThat(body.get("token")).isEqualTo("device-token");
        assertThat(body.get("notification"))
                .isEqualTo(Map.of("title", message.title(), "body", message.body()));
        // 딥링크는 포그라운드·백그라운드 어느 경로로 오든 앱이 읽어야 해서 data 로도 실린다.
        assertThat(body.get("data")).isEqualTo(message.data());
        assertThat(body).containsKeys("apns", "android");
    }

    @Test
    @SuppressWarnings("unchecked")
    void android_블록은_채널과_우선순위를_지정한다() {
        Map<String, Object> android = (Map<String, Object>) messageBody().get("android");
        Map<String, Object> notification = (Map<String, Object>) android.get("notification");

        assertThat(android.get("priority")).isEqualTo("high");
        assertThat(notification.get("default_sound")).isEqualTo(true);
        // 앱(nativePush.ts 의 ANDROID_CHANNEL_ID)이 만드는 채널과 같아야 한다.
        assertThat(notification.get("channel_id")).isEqualTo("dearbloom-default");
    }

    @Test
    @SuppressWarnings("unchecked")
    void apns_블록은_즉시_표시와_소리를_지정한다() {
        Map<String, Object> apns = (Map<String, Object>) messageBody().get("apns");
        Map<String, Object> headers = (Map<String, Object>) apns.get("headers");
        Map<String, Object> payload = (Map<String, Object>) apns.get("payload");
        Map<String, Object> aps = (Map<String, Object>) payload.get("aps");

        assertThat(headers.get("apns-priority")).isEqualTo("10");
        assertThat(aps.get("sound")).isEqualTo("default");
        // 무음 백그라운드 갱신용이라 넣지 않는다 — 넣으면 iOS 가 전송을 조절할 수 있다.
        assertThat(aps).doesNotContainKey("content-available");
    }
}
