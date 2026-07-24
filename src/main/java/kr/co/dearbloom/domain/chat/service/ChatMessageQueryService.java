package kr.co.dearbloom.domain.chat.service;

import kr.co.dearbloom.domain.chat.entity.ChatMessage;
import kr.co.dearbloom.domain.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatMessageQueryService {
    private final ChatMessageRepository chatMessageRepository;

    /** 방 메시지 히스토리(최신 id 부터 size 개). cursor=null 이면 최신부터, 이후엔 받은 마지막 id 를 cursor 로. */
    public List<ChatMessage> getHistory(Long roomId, Long cursor, int size) {
        return chatMessageRepository.findHistory(roomId, cursor, PageRequest.of(0, size));
    }
}
