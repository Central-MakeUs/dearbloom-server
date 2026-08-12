package kr.co.dearbloom.domain.notification.service;

import kr.co.dearbloom.domain.notification.entity.DeviceToken;
import kr.co.dearbloom.domain.notification.message.PushMessage;
import kr.co.dearbloom.global.push.PushSendResult;
import kr.co.dearbloom.global.push.PushSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 수신자의 기기 전체에 푸시를 보내고 죽은 토큰을 정리한다.
 *
 * <p><b>알림함이 없어서 발송 로그가 유일한 추적 수단이다.</b> "알림이 안 왔어요" 문의가 오면
 * 이 로그로만 확인할 수 있으므로, 수신자·종류·대상 건·결과를 반드시 남긴다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PushNotificationService {
    private final DeviceTokenQueryService deviceTokenQueryService;
    private final DeviceTokenCommandService deviceTokenCommandService;
    private final PushSender pushSender;

    /**
     * @param kind 로그용 알림 종류 식별자 (예: {@code INQUIRY_CREATED})
     * @param referenceId 로그용 대상 건 식별자 (문의 ID)
     */
    public void sendToMember(Long memberId, PushMessage message, String kind, Long referenceId) {
        List<DeviceToken> targets = deviceTokenQueryService.findSendTargets(memberId);
        if (targets.isEmpty()) {
            log.info("[Push] 대상 기기 없음 — memberId={}, kind={}, refId={}", memberId, kind, referenceId);
            return;
        }

        for (DeviceToken target : targets) {
            PushSendResult result = pushSender.send(target.getToken(), message);
            log.info("[Push] memberId={}, kind={}, refId={}, result={}",
                    memberId, kind, referenceId, result);

            if (result.shouldDeleteToken()) {
                deviceTokenCommandService.delete(target.getToken());
            }
        }
    }
}
