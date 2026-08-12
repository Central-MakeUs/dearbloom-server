package kr.co.dearbloom.domain.notification.message;

import java.util.Map;

/**
 * 플랫폼 중립 푸시 메시지. 발송 어댑터가 벤더(FCM 등) 포맷으로 변환한다.
 *
 * @param title 알림 제목
 * @param body  알림 본문. <b>잠금화면에 그대로 노출되므로 실명·학교 등 개인정보를 넣지 않는다.</b>
 * @param data  앱이 읽을 부가 데이터. {@code deepLink} 는 셸이 WebView 에 로드할 경로다.
 */
public record PushMessage(String title, String body, Map<String, String> data) {
    public static final String DEEP_LINK_KEY = "deepLink";

    public static PushMessage of(String title, String body, String deepLink) {
        return new PushMessage(title, body, Map.of(DEEP_LINK_KEY, deepLink));
    }

    public String deepLink() {
        return data.get(DEEP_LINK_KEY);
    }
}
