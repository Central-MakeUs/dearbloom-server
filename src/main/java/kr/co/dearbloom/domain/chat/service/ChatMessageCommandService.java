package kr.co.dearbloom.domain.chat.service;

import kr.co.dearbloom.domain.chat.entity.ChatMessage;
import kr.co.dearbloom.domain.chat.entity.ChatRoom;
import kr.co.dearbloom.domain.chat.repository.ChatMessageRepository;
import kr.co.dearbloom.domain.inquiry.entity.Inquiry;
import kr.co.dearbloom.domain.member.entity.MemberRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatMessageCommandService {
    private final ChatMessageRepository chatMessageRepository;

    public ChatMessage saveText(ChatRoom room, MemberRole senderRole, String content) {
        return chatMessageRepository.save(ChatMessage.text(room, senderRole, content));
    }

    public ChatMessage saveImage(ChatRoom room, MemberRole senderRole, String imageUrl) {
        return chatMessageRepository.save(ChatMessage.image(room, senderRole, imageUrl));
    }

    public ChatMessage saveInquiryCard(ChatRoom room, MemberRole senderRole, Inquiry inquiry) {
        return chatMessageRepository.save(ChatMessage.inquiryCard(room, senderRole, inquiry));
    }
}
