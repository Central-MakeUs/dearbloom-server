package kr.co.dearbloom.domain.chat.ws;

import kr.co.dearbloom.domain.chat.repository.ChatRoomRepository;
import kr.co.dearbloom.domain.member.entity.MemberRole;
import kr.co.dearbloom.global.auth.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * STOMP 인증/인가 인터셉터.
 * - CONNECT: 핸드셰이크에서 받은 accessToken 쿠키({@link CookieTokenHandshakeInterceptor})를 검증해
 *   (memberId, activeRole, profileId) Principal 세팅. 실패 시 연결 거부.
 * - SUBSCRIBE /topic/rooms/{roomId}: 그 방의 참여자만 구독 허용(타인 방 도청 방지).
 * 인터셉터는 트랜잭션 밖이라 LAZY 없이 참여자 PK만 조회해 검증한다.
 */
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {
    private static final String ROOM_PREFIX = "/topic/rooms/";

    private final TokenProvider tokenProvider;
    private final ChatRoomRepository chatRoomRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getCommand();
        if (StompCommand.CONNECT.equals(command)) {
            accessor.setUser(authenticate(accessor));
        } else if (StompCommand.SUBSCRIBE.equals(command)) {
            authorizeSubscription(accessor);
        }
        return message;
    }

    private ChatPrincipal authenticate(StompHeaderAccessor accessor) {
        String token = resolveToken(accessor);
        if (token == null || !tokenProvider.validToken(token)) {
            throw new MessagingException("유효하지 않은 토큰입니다.");
        }
        MemberRole role = tokenProvider.getActiveRole(token);
        Long profileId = tokenProvider.getActiveProfileId(token);
        if (role == null || profileId == null) {
            throw new MessagingException("활성 역할 정보가 없습니다.");
        }
        return new ChatPrincipal(tokenProvider.getMemberId(token), role, profileId);
    }

    private void authorizeSubscription(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null || !destination.startsWith(ROOM_PREFIX)) {
            return; // 방 토픽이 아니면 검증 대상 아님
        }
        Long roomId = parseRoomId(destination);
        if (!(accessor.getUser() instanceof ChatPrincipal principal)) {
            throw new MessagingException("인증되지 않은 구독입니다.");
        }
        ChatRoomRepository.Participants participants = chatRoomRepository.findParticipants(roomId)
                .orElseThrow(() -> new MessagingException("채팅방을 찾을 수 없습니다."));
        Long myId = principal.role() == MemberRole.CUSTOMER
                ? participants.getCustomerId()
                : participants.getArtistId();
        if (!principal.profileId().equals(myId)) {
            throw new MessagingException("해당 채팅방 구독 권한이 없습니다.");
        }
    }

    /**
     * 핸드셰이크에서 쿠키로 받아둔 토큰을 쓴다. 클라이언트가 웹(웹뷰 포함)이라 토큰이 httpOnly 쿠키이고,
     * JS 가 읽을 수 없어 CONNECT 프레임 헤더에는 실을 수 없다.
     */
    private String resolveToken(StompHeaderAccessor accessor) {
        Map<String, Object> attributes = accessor.getSessionAttributes();
        if (attributes == null) {
            return null;
        }
        return attributes.get(CookieTokenHandshakeInterceptor.ACCESS_TOKEN_ATTRIBUTE) instanceof String token
                ? token
                : null;
    }

    private Long parseRoomId(String destination) {
        try {
            return Long.parseLong(destination.substring(ROOM_PREFIX.length()));
        } catch (NumberFormatException e) {
            throw new MessagingException("잘못된 채팅방 경로입니다.");
        }
    }
}
