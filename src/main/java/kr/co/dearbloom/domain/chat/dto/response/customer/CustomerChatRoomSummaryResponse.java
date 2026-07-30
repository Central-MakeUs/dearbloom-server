package kr.co.dearbloom.domain.chat.dto.response.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.dearbloom.domain.chat.entity.ChatRoom;
import kr.co.dearbloom.domain.member.entity.MemberRole;

import java.time.LocalDateTime;

@Schema(description = "고객 채팅방 목록 항목")
public record CustomerChatRoomSummaryResponse(
        @Schema(description = "채팅방 ID", example = "1")
        Long roomId,

        @Schema(description = "작가 닉네임", example = "블루밍데이즈 스냅")
        String artistNickname,

        @Schema(description = "작가 프로필 이미지 URL", nullable = true)
        String artistImageUrl,

        @Schema(description = "마지막 메시지 미리보기", example = "안녕하세요 문의 내용 확인 했습니다!")
        String lastMessagePreview,

        @Schema(description = "마지막 메시지 시각", example = "2026-06-11T09:00:00")
        LocalDateTime lastMessageAt,

        @Schema(description = "안읽음 수", example = "1")
        int unreadCount
) {
    public static CustomerChatRoomSummaryResponse of(ChatRoom room) {
        return new CustomerChatRoomSummaryResponse(
                room.getChatRoomId(),
                room.getArtist().getNickname(),
                room.getArtist().getImageUrl(),
                room.getLastMessagePreview(),
                room.getLastMessageAt(),
                room.unreadFor(MemberRole.CUSTOMER));
    }
}
