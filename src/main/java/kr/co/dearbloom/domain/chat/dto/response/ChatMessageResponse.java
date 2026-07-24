package kr.co.dearbloom.domain.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.dearbloom.domain.chat.entity.ChatMessage;
import kr.co.dearbloom.domain.chat.entity.ChatMessageType;
import kr.co.dearbloom.domain.member.entity.MemberRole;

import java.time.LocalDateTime;

/** 채팅 메시지 1건. 타입에 따라 하나만 채워진다 — TEXT=content, IMAGE=imageUrl, INQUIRY=inquiryCard. */
@Schema(description = "채팅 메시지")
public record ChatMessageResponse(
        @Schema(description = "메시지 ID", example = "100")
        Long messageId,

        @Schema(description = "발신자 역할", example = "CUSTOMER")
        MemberRole senderRole,

        @Schema(description = "메시지 종류", example = "TEXT")
        ChatMessageType messageType,

        @Schema(description = "TEXT 본문 (그 외 타입이면 null)", example = "네 감사합니다!")
        String content,

        @Schema(description = "IMAGE 사진 URL (그 외 타입이면 null)",
                example = "https://cdn.dearbloom.co.kr/chat/image/abc.webp")
        String imageUrl,

        @Schema(description = "INQUIRY 카드 (그 외 타입이면 null)")
        InquiryCardResponse inquiryCard,

        @Schema(description = "발신 시각", example = "2026-06-11T09:00:00")
        LocalDateTime createdAt
) {
    public static ChatMessageResponse from(ChatMessage message) {
        InquiryCardResponse card = (message.getMessageType() == ChatMessageType.INQUIRY && message.getInquiry() != null)
                ? InquiryCardResponse.from(message.getInquiry())
                : null;
        return new ChatMessageResponse(
                message.getChatMessageId(),
                message.getSenderRole(),
                message.getMessageType(),
                message.getContent(),
                message.getImageUrl(),
                card,
                message.getCreatedAt());
    }
}
