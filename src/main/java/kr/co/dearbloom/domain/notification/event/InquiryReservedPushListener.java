package kr.co.dearbloom.domain.notification.event;

import kr.co.dearbloom.domain.inquiry.event.InquiryReservedPushEvent;
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
 * 예약 완료 → 고객에게 푸시. 실행 시점·예외 처리 원칙은 {@link InquiryCreatedPushListener} 와 같다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InquiryReservedPushListener {
    private static final String KIND = "INQUIRY_RESERVED";

    private final PushNotificationService pushNotificationService;
    private final PushMessageFactory pushMessageFactory;

    @Async(AsyncConfig.PUSH_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onInquiryReserved(InquiryReservedPushEvent event) {
        try {
            pushNotificationService.sendToMember(
                    event.customerMemberId(),
                    pushMessageFactory.inquiryReserved(
                            event.inquiryId(), event.artworkName(), event.shootDate(), event.startTime()),
                    KIND,
                    event.inquiryId());
        } catch (Exception e) {
            log.warn("[Push] {} 발송 실패 — inquiryId={}, {}", KIND, event.inquiryId(), e.getMessage());
        }
    }
}
