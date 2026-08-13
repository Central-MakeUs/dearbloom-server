package kr.co.dearbloom.domain.chat.dto.response.artist;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.dearbloom.domain.chat.entity.ChatRoom;
import kr.co.dearbloom.domain.customer.entity.CustomerProfileImage;
import kr.co.dearbloom.domain.member.entity.MemberRole;

import java.time.LocalDateTime;

@Schema(description = "작가 채팅방 목록 항목")
public record ArtistChatRoomSummaryResponse(
        @Schema(description = "채팅방 ID", example = "1")
        Long roomId,

        @Schema(description = "고객명", example = "김디어")
        String customerName,

        @Schema(description = "고객 기본 프로필 이미지 색상(온보딩 시 자동 배정)", example = "GREEN")
        CustomerProfileImage customerProfileColor,

        @Schema(description = "마지막 메시지 미리보기", example = "안녕하세요 문의 내용 확인 했습니다!")
        String lastMessagePreview,

        @Schema(description = "마지막 메시지 시각", example = "2026-06-11T09:00:00")
        LocalDateTime lastMessageAt,

        @Schema(description = "안읽음 수", example = "1")
        int unreadCount
) {
    public static ArtistChatRoomSummaryResponse of(ChatRoom room) {
        return new ArtistChatRoomSummaryResponse(
                room.getChatRoomId(),
                room.getCustomer().getName(),
                room.getCustomer().getProfileColor(),
                room.getLastMessagePreview(),
                room.getLastMessageAt(),
                room.unreadFor(MemberRole.ARTIST));
    }
}
