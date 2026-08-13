package kr.co.dearbloom.global.push;

import kr.co.dearbloom.domain.notification.message.PushMessage;

/**
 * 푸시 발송 어댑터. 벤더(FCM → APNs 직접 등) 교체에 대비해 인터페이스로 분리한다.
 * 구현체는 예외를 던지지 않고 {@link PushSendResult} 로만 답한다 — 푸시 실패가 비즈니스 흐름을 깨면 안 되기 때문.
 */
public interface PushSender {
    PushSendResult send(String deviceToken, PushMessage message);
}
