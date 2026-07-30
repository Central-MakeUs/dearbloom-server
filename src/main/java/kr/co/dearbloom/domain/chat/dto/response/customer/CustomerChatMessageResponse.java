package kr.co.dearbloom.domain.chat.dto.response.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.dearbloom.domain.chat.dto.response.InquiryCardResponse;
import kr.co.dearbloom.domain.chat.entity.ChatMessage;
import kr.co.dearbloom.domain.chat.entity.ChatMessageType;
import kr.co.dearbloom.domain.chat.entity.ChatRoom;
import kr.co.dearbloom.domain.member.entity.MemberRole;

import java.time.LocalDateTime;

/** 고객이 보는 채팅 메시지 1건. 타입에 따라 하나만 채워진다 — TEXT=content, IMAGE=imageUrl, INQUIRY=inquiryCard. */
@Schema(description = "고객 채팅 메시지")
public record CustomerChatMessageResponse(
        @Schema(description = "메시지 ID", example = "100")
        Long messageId,

        @Schema(description = "발신자 역할", example = "ARTIST")
        MemberRole senderRole,

        @Schema(description = "작가 닉네임", example = "블루밍데이즈 스냅")
        String artistNickname,

        @Schema(description = "작가 프로필 이미지 URL", nullable = true)
        String artistImageUrl,

        @Schema(description = "메시지 종류", example = "TEXT")
        ChatMessageType messageType,

        @Schema(description = "TEXT 본문 (그 외 타입이면 null)", example = "네 감사합니다!")
        String content,

        @Schema(description = "IMAGE 사진 URL (그 외 타입이면 null)",
                example = "https://dev-cdn.dearbloom.co.kr/chat/image/abc.webp")
        String imageUrl,

        @Schema(description = "INQUIRY 카드 (그 외 타입이면 null)")
        InquiryCardResponse inquiryCard,

        @Schema(description = "발신 시각", example = "2026-06-11T09:00:00")
        LocalDateTime createdAt
) {
    /** 작가 정보는 방 기준이라 메시지마다 같은 값이 들어간다(말풍선 옆 프로필 렌더용). */
    public static CustomerChatMessageResponse of(ChatMessage message, ChatRoom room) {
        InquiryCardResponse card = (message.getMessageType() == ChatMessageType.INQUIRY && message.getInquiry() != null)
                ? InquiryCardResponse.from(message.getInquiry())
                : null;
        return new CustomerChatMessageResponse(
                message.getChatMessageId(),
                message.getSenderRole(),
                room.getArtist().getNickname(),
                room.getArtist().getImageUrl(),
                message.getMessageType(),
                message.getContent(),
                message.getImageUrl(),
                card,
                message.getCreatedAt());
    }
}
