package kr.co.dearbloom.domain.notification.event;

import kr.co.dearbloom.domain.inquiry.event.InquiryCreatedPushEvent;
import kr.co.dearbloom.domain.notification.message.PushMessageFactory;
import kr.co.dearbloom.domain.notification.service.PushNotificationService;
import kr.co.dearbloom.global.config.AsyncConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 문의 생성 → 작가에게 푸시.
 *
 * <p>{@code AFTER_COMMIT} + {@code @Async} 다. 커밋 이후에 돌아야 롤백된 문의의 알림이 나가지 않고,
 * 비동기여야 FCM 응답을 기다리는 동안 문의 API 응답이 늦어지지 않는다.
 *
 * <p><b>예외를 삼킨다.</b> 푸시 실패가 이미 커밋된 문의에 영향을 주면 안 된다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InquiryCreatedPushListener {
    private static final String KIND = "INQUIRY_CREATED";

    private final PushNotificationService pushNotificationService;
    private final PushMessageFactory pushMessageFactory;

    @Async(AsyncConfig.PUSH_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onInquiryCreated(InquiryCreatedPushEvent event) {
        try {
            pushNotificationService.sendToMember(
                    event.artistMemberId(),
                    pushMessageFactory.inquiryCreated(
                            event.inquiryId(), event.artworkName(), event.shootDate(), event.startTime()),
                    KIND,
                    event.inquiryId());
        } catch (Exception e) {
            log.warn("[Push] {} 발송 실패 — inquiryId={}, {}", KIND, event.inquiryId(), e.getMessage());
        }
    }
}
