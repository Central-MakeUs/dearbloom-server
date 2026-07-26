package kr.co.dearbloom.domain.chat.entity;

import jakarta.persistence.*;
import kr.co.dearbloom.domain.inquiry.entity.Inquiry;
import kr.co.dearbloom.domain.member.entity.MemberRole;
import kr.co.dearbloom.global.entity.BaseTime;
import lombok.*;

/**
 * 채팅 메시지(append-only). createdAt(BaseTime)=발신 시각.
 * TEXT 는 content 사용, INQUIRY 는 inquiry 참조(카드 렌더는 문의 스냅샷에서 조립, content 는 null).
 * 발신자는 방에 각 1명뿐이라 senderRole 만으로 특정된다.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
@Table(indexes = @Index(name = "idx_chat_message_room", columnList = "chat_room_id, chat_message_id"))
public class ChatMessage extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chatMessageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole senderRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatMessageType messageType;

    // TEXT 메시지 본문. TEXT 외에는 null.
    @Column(columnDefinition = "TEXT")
    private String content;

    // IMAGE 메시지의 사진 URL(CDN). IMAGE 외에는 null. content 와 상호배타(둘 다 채우지 않음).
    private String imageUrl;

    // INQUIRY 카드가 참조하는 문의. INQUIRY 외에는 null.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiry_id")
    private Inquiry inquiry;

    public static ChatMessage text(ChatRoom chatRoom, MemberRole senderRole, String content) {
        return ChatMessage.builder()
                .chatRoom(chatRoom)
                .senderRole(senderRole)
                .messageType(ChatMessageType.TEXT)
                .content(content)
                .build();
    }

    public static ChatMessage image(ChatRoom chatRoom, MemberRole senderRole, String imageUrl) {
        return ChatMessage.builder()
                .chatRoom(chatRoom)
                .senderRole(senderRole)
                .messageType(ChatMessageType.IMAGE)
                .imageUrl(imageUrl)
                .build();
    }

    public static ChatMessage inquiryCard(ChatRoom chatRoom, MemberRole senderRole, Inquiry inquiry) {
        return ChatMessage.builder()
                .chatRoom(chatRoom)
                .senderRole(senderRole)
                .messageType(ChatMessageType.INQUIRY)
                .inquiry(inquiry)
                .build();
    }
}
