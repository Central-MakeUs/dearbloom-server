package kr.co.dearbloom.domain.notification.service;

import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.domain.notification.entity.DevicePlatform;
import kr.co.dearbloom.domain.notification.entity.DeviceToken;
import kr.co.dearbloom.domain.notification.message.PushMessage;
import kr.co.dearbloom.global.push.PushSendResult;
import kr.co.dearbloom.global.push.PushSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * 발송 후 죽은 토큰 정리 규칙 검증.
 * FCM 은 무효 토큰을 응답으로 즉시 알려주므로, 그 자리에서 지우고 폴링 스케줄러를 두지 않는다.
 */
@ExtendWith(MockitoExtension.class)
class PushNotificationServiceTest {
    private static final Long MEMBER_ID = 1L;
    private static final Long INQUIRY_ID = 100L;
    private static final String KIND = "INQUIRY_CREATED";

    @Mock DeviceTokenQueryService deviceTokenQueryService;
    @Mock DeviceTokenCommandService deviceTokenCommandService;
    @Mock PushSender pushSender;
    @InjectMocks PushNotificationService pushNotificationService;

    private final PushMessage message = PushMessage.of("제목", "본문", "/app/artist/requests/100");

    private DeviceToken token(String value) {
        return DeviceToken.builder()
                .member(Member.builder().email("user@example.com").name("user").build())
                .token(value)
                .platform(DevicePlatform.IOS)
                .build();
    }

    @Test
    void 무효_토큰은_발송_직후_삭제한다() {
        given(deviceTokenQueryService.findSendTargets(MEMBER_ID)).willReturn(List.of(token("dead")));
        given(pushSender.send("dead", message)).willReturn(PushSendResult.TOKEN_INVALID);

        pushNotificationService.sendToMember(MEMBER_ID, message, KIND, INQUIRY_ID);

        verify(deviceTokenCommandService).delete("dead");
    }

    @Test
    void 성공한_토큰은_삭제하지_않는다() {
        given(deviceTokenQueryService.findSendTargets(MEMBER_ID)).willReturn(List.of(token("alive")));
        given(pushSender.send("alive", message)).willReturn(PushSendResult.SUCCESS);

        pushNotificationService.sendToMember(MEMBER_ID, message, KIND, INQUIRY_ID);

        verify(deviceTokenCommandService, never()).delete("alive");
    }

    @Test
    void 일시_오류는_토큰을_유지한다() {
        given(deviceTokenQueryService.findSendTargets(MEMBER_ID)).willReturn(List.of(token("busy")));
        given(pushSender.send("busy", message)).willReturn(PushSendResult.RETRYABLE_FAILURE);

        pushNotificationService.sendToMember(MEMBER_ID, message, KIND, INQUIRY_ID);

        verify(deviceTokenCommandService, never()).delete("busy");
    }

    @Test
    void 기기가_여러_대면_모두에게_보내고_죽은_것만_지운다() {
        given(deviceTokenQueryService.findSendTargets(MEMBER_ID))
                .willReturn(List.of(token("alive"), token("dead")));
        given(pushSender.send("alive", message)).willReturn(PushSendResult.SUCCESS);
        given(pushSender.send("dead", message)).willReturn(PushSendResult.TOKEN_INVALID);

        pushNotificationService.sendToMember(MEMBER_ID, message, KIND, INQUIRY_ID);

        verify(pushSender).send("alive", message);
        verify(deviceTokenCommandService).delete("dead");
        verify(deviceTokenCommandService, never()).delete("alive");
    }

    @Test
    void 등록된_기기가_없으면_발송을_시도하지_않는다() {
        given(deviceTokenQueryService.findSendTargets(MEMBER_ID)).willReturn(List.of());

        pushNotificationService.sendToMember(MEMBER_ID, message, KIND, INQUIRY_ID);

        verifyNoInteractions(pushSender);
    }
}
