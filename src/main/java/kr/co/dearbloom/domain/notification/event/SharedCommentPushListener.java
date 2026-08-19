package kr.co.dearbloom.domain.notification.event;

import kr.co.dearbloom.domain.board.event.SharedCommentCreatedPushEvent;
import kr.co.dearbloom.domain.board.service.board.SharedMemberQueryService;
import kr.co.dearbloom.domain.notification.message.PushMessage;
import kr.co.dearbloom.domain.notification.message.PushMessageFactory;
import kr.co.dearbloom.domain.notification.service.PushNotificationService;
import kr.co.dearbloom.global.config.AsyncConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

/**
 * 공동보드 댓글 → 작성자를 뺀 참여자 전원에게 푸시.
 *
 * <p><b>{@code AFTER_COMMIT} + {@code @Async} 다.</b> 커밋 이후에 돌아야 롤백된 댓글의 알림이 나가지 않고,
 * 비동기여야 참여자 수만큼의 FCM 왕복을 기다리느라 댓글 등록 API 가 늦어지지 않는다.
 * AFTER_COMMIT 은 이미 커밋된 트랜잭션이라 참여할 수 없으므로 {@code REQUIRES_NEW} 로 새로 연다.
 *
 * <p>수신자는 여기서 조회한다 — 댓글 등록 트랜잭션에 조회를 얹지 않기 위해서다.
 *
 * <p><b>예외를 삼킨다.</b> 알림 실패가 이미 커밋된 댓글에 영향을 주면 안 된다.
 * 한 명에게 실패해도 나머지는 계속 보낸다 — 기기 하나가 망가졌다고 보드 전체가 알림을 못 받으면 안 된다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SharedCommentPushListener {
    private static final String KIND = "SHARED_COMMENT_CREATED";

    private final SharedMemberQueryService sharedMemberQueryService;
    private final PushNotificationService pushNotificationService;
    private final PushMessageFactory pushMessageFactory;

    @Async(AsyncConfig.PUSH_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void onSharedCommentCreated(SharedCommentCreatedPushEvent event) {
        List<Long> recipients;
        try {
            recipients = sharedMemberQueryService.getOtherMemberIds(
                    event.sharedBoardId(), event.authorCustomerId());
        } catch (Exception e) {
            log.warn("[Push] {} 수신자 조회 실패 — sharedBoardId={}, {}",
                    KIND, event.sharedBoardId(), e.getMessage());
            return;
        }
        if (recipients.isEmpty()) {
            // 혼자 쓰는 보드다. 정상 상황이라 경고로 올리지 않는다.
            log.info("[Push] {} 수신자 없음 — sharedBoardId={}", KIND, event.sharedBoardId());
            return;
        }

        PushMessage message = pushMessageFactory.sharedCommentCreated(
                event.sharedBoardId(), event.authorName(), event.content());

        for (Long memberId : recipients) {
            try {
                pushNotificationService.sendToMember(memberId, message, KIND, event.sharedBoardId());
            } catch (Exception e) {
                log.warn("[Push] {} 발송 실패 — memberId={}, sharedBoardId={}, {}",
                        KIND, memberId, event.sharedBoardId(), e.getMessage());
            }
        }
    }
}
