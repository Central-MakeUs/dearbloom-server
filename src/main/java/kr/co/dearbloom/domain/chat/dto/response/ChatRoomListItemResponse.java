package kr.co.dearbloom.domain.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.dearbloom.domain.chat.entity.ChatRoom;
import kr.co.dearbloom.domain.member.entity.MemberRole;

import java.time.LocalDateTime;

/** 채팅 목록 1행. 상대방(고객이면 작가, 작가면 고객) 표시 정보 + 마지막 메시지 미리보기·안읽음 수. */
@Schema(description = "채팅방 목록 항목")
public record ChatRoomListItemResponse(
        @Schema(description = "채팅방 ID", example = "1")
        Long roomId,

        @Schema(description = "상대방 이름 (작가 닉네임 또는 고객명)", example = "블루밍데이즈 스냅")
        String counterpartyName,

        @Schema(description = "상대방 프로필 이미지 URL (고객은 이미지 없어 null 일 수 있음)", nullable = true)
        String counterpartyImageUrl,

        @Schema(description = "마지막 메시지 미리보기", example = "안녕하세요 문의 내용 확인 했습니다!")
        String lastMessagePreview,

        @Schema(description = "마지막 메시지 시각", example = "2026-06-11T09:00:00")
        LocalDateTime lastMessageAt,

        @Schema(description = "안읽음 수", example = "1")
        int unreadCount
) {
    /** viewerRole 관점으로 상대방 정보를 뽑아 매핑. (fetch join 된 상대 프로필 접근) */
    public static ChatRoomListItemResponse of(ChatRoom room, MemberRole viewerRole) {
        String name;
        String imageUrl;
        if (viewerRole == MemberRole.CUSTOMER) {
            name = room.getArtist().getNickname();
            imageUrl = room.getArtist().getImageUrl();
        } else {
            name = room.getCustomer().getName();
            imageUrl = null;
        }
        return new ChatRoomListItemResponse(
                room.getChatRoomId(),
                name,
                imageUrl,
                room.getLastMessagePreview(),
                room.getLastMessageAt(),
                room.unreadFor(viewerRole));
    }
}
