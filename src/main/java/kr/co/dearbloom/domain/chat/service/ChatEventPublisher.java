package kr.co.dearbloom.domain.chat.service;

import kr.co.dearbloom.domain.chat.dto.response.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/** 새 메시지를 방 구독자(/topic/rooms/{roomId})에게 실시간 브로드캐스트한다. */
@Component
@RequiredArgsConstructor
public class ChatEventPublisher {
    private static final String ROOM_DESTINATION = "/topic/rooms/";

    private final SimpMessagingTemplate messagingTemplate;

    public void sendToRoom(Long roomId, ChatMessageResponse message) {
        messagingTemplate.convertAndSend(ROOM_DESTINATION + roomId, message);
    }
}
