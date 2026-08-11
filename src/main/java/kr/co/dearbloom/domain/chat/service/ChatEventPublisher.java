package kr.co.dearbloom.domain.chat.service;

import kr.co.dearbloom.domain.chat.dto.response.ChatMessageResponse;
import kr.co.dearbloom.domain.chat.dto.response.ChatReadEventResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * 방 구독자에게 실시간 브로드캐스트한다.
 * 새 메시지는 /topic/rooms/{roomId}, 읽음 이벤트는 /topic/rooms/{roomId}/read 로 나간다
 * (기존 메시지 스트림의 페이로드 형태를 바꾸지 않으려고 목적지를 분리했다).
 */
@Component
@RequiredArgsConstructor
public class ChatEventPublisher {
    private static final String ROOM_DESTINATION = "/topic/rooms/";
    private static final String READ_SUFFIX = "/read";

    private final SimpMessagingTemplate messagingTemplate;

    public void sendToRoom(Long roomId, ChatMessageResponse message) {
        messagingTemplate.convertAndSend(ROOM_DESTINATION + roomId, message);
    }

    /** 읽음 이벤트. 방 전체에 나가므로 읽은 본인에게도 도달한다(수신 측이 readerRole 로 걸러낸다). */
    public void sendReadToRoom(Long roomId, ChatReadEventResponse event) {
        messagingTemplate.convertAndSend(ROOM_DESTINATION + roomId + READ_SUFFIX, event);
    }
}
