package kr.co.dearbloom.domain.chat.service;

import kr.co.dearbloom.domain.chat.entity.ChatRoom;
import kr.co.dearbloom.domain.chat.repository.ChatRoomRepository;
import kr.co.dearbloom.domain.member.entity.MemberRole;
import kr.co.dearbloom.global.dto.response.exception.CustomException;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomQueryService {
    private final ChatRoomRepository chatRoomRepository;

    /** viewer 역할 기준 방 목록(상대 프로필 fetch join, 최근 메시지순). */
    public List<ChatRoom> getMyRooms(MemberRole role, Long profileId) {
        return role == MemberRole.CUSTOMER
                ? chatRoomRepository.findCustomerRooms(profileId)
                : chatRoomRepository.findArtistRooms(profileId);
    }

    /** 방 조회 + 참여자 검증. 없으면 404, 참여자 아니면 403. */
    public ChatRoom getParticipatingRoom(Long roomId, MemberRole role, Long profileId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));
        if (!room.isParticipant(role, profileId)) {
            throw new CustomException(ErrorCode.CHAT_ACCESS_DENIED);
        }
        return room;
    }
}
