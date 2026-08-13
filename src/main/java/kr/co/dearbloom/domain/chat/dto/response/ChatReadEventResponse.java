package kr.co.dearbloom.domain.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.dearbloom.domain.member.entity.MemberRole;

import java.time.LocalDateTime;

/**
 * 읽음 이벤트. 상대가 방을 읽는 즉시 {@code /topic/rooms/{roomId}/read} 로 브로드캐스트된다.
 * 수신 측은 "내가 보낸 메시지 중 createdAt <= readAt" 인 것들을 읽음으로 갱신하면 된다(재조회 불필요).
 */
@Schema(description = "채팅 읽음 이벤트")
public record ChatReadEventResponse(
        @Schema(description = "읽은 사람의 역할. 내 역할과 같으면 무시(내가 읽은 것).", example = "ARTIST")
        MemberRole readerRole,

        @Schema(description = "읽은 시각. 이 시각 이전에 보낸 메시지는 모두 읽힌 것으로 본다.",
                example = "2026-06-11T09:30:00")
        LocalDateTime readAt
) {
}
