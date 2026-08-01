package kr.co.dearbloom.domain.chat.facade;

import kr.co.dearbloom.domain.artist.entity.artist.Artist;
import kr.co.dearbloom.domain.chat.dto.response.ChatMessageResponse;
import kr.co.dearbloom.domain.chat.dto.response.artist.ArtistChatMessageResponse;
import kr.co.dearbloom.domain.chat.dto.response.artist.ArtistChatRoomSummaryResponse;
import kr.co.dearbloom.domain.chat.dto.response.customer.CustomerChatMessageResponse;
import kr.co.dearbloom.domain.chat.dto.response.customer.CustomerChatRoomSummaryResponse;
import kr.co.dearbloom.domain.chat.entity.ChatMessage;
import kr.co.dearbloom.domain.chat.entity.ChatRoom;
import kr.co.dearbloom.domain.chat.service.ChatEventPublisher;
import kr.co.dearbloom.domain.chat.service.ChatMessageCommandService;
import kr.co.dearbloom.domain.chat.service.ChatMessageQueryService;
import kr.co.dearbloom.domain.chat.service.ChatRoomCommandService;
import kr.co.dearbloom.domain.chat.service.ChatRoomQueryService;
import kr.co.dearbloom.domain.customer.entity.Customer;
import kr.co.dearbloom.domain.inquiry.entity.Inquiry;
import kr.co.dearbloom.domain.member.entity.MemberRole;
import kr.co.dearbloom.global.file.FileUrlValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** 채팅 유스케이스 — 목록/히스토리/전송/읽음 + 문의 생성 시 방·카드 자동 생성. */
@Component
@RequiredArgsConstructor
public class ChatFacade {
    private static final int DEFAULT_PAGE_SIZE = 30;
    private static final int MAX_PAGE_SIZE = 100;
    private static final int PREVIEW_MAX = 50;
    private static final String IMAGE_PREVIEW = "[사진]";

    private final ChatRoomCommandService chatRoomCommandService;
    private final ChatRoomQueryService chatRoomQueryService;
    private final ChatMessageCommandService chatMessageCommandService;
    private final ChatMessageQueryService chatMessageQueryService;
    private final ChatEventPublisher chatEventPublisher;
    private final FileUrlValidator fileUrlValidator;

    /** 고객의 채팅 목록. */
    @Transactional(readOnly = true)
    public List<CustomerChatRoomSummaryResponse> getCustomerRooms(Long customerId) {
        return chatRoomQueryService.getMyRooms(MemberRole.CUSTOMER, customerId).stream()
                .map(CustomerChatRoomSummaryResponse::of)
                .toList();
    }

    /** 작가의 채팅 목록. */
    @Transactional(readOnly = true)
    public List<ArtistChatRoomSummaryResponse> getArtistRooms(Long artistId) {
        return chatRoomQueryService.getMyRooms(MemberRole.ARTIST, artistId).stream()
                .map(ArtistChatRoomSummaryResponse::of)
                .toList();
    }

    /** 고객의 메시지 히스토리(권한 검증 후 최신 size 개, 화면 표시용 오름차순). */
    @Transactional(readOnly = true)
    public List<CustomerChatMessageResponse> getCustomerMessages(Long customerId, Long roomId, Long cursor, Integer size) {
        ChatRoom room = chatRoomQueryService.getParticipatingRoom(roomId, MemberRole.CUSTOMER, customerId);
        return historyAscending(roomId, cursor, size).stream()
                .map(message -> CustomerChatMessageResponse.of(message, room))
                .toList();
    }

    /** 작가의 메시지 히스토리(권한 검증 후 최신 size 개, 화면 표시용 오름차순). */
    @Transactional(readOnly = true)
    public List<ArtistChatMessageResponse> getArtistMessages(Long artistId, Long roomId, Long cursor, Integer size) {
        ChatRoom room = chatRoomQueryService.getParticipatingRoom(roomId, MemberRole.ARTIST, artistId);
        return historyAscending(roomId, cursor, size).stream()
                .map(message -> ArtistChatMessageResponse.of(message, room))
                .toList();
    }

    private List<ChatMessage> historyAscending(Long roomId, Long cursor, Integer size) {
        int pageSize = (size == null || size <= 0 || size > MAX_PAGE_SIZE) ? DEFAULT_PAGE_SIZE : size;
        List<ChatMessage> messages = chatMessageQueryService.getHistory(roomId, cursor, pageSize);
        List<ChatMessage> result = new ArrayList<>(messages.size());
        for (int i = messages.size() - 1; i >= 0; i--) { // DESC → 오름차순
            result.add(messages.get(i));
        }
        return result;
    }

    /** 텍스트 전송. 권한 검증 → 저장 → 방 미리보기·안읽음 갱신 → 구독자 브로드캐스트. 고객·작가 동작이 같아 role 만 받는다. */
    @Transactional
    public ChatMessageResponse sendText(MemberRole role, Long profileId, Long roomId, String content) {
        ChatRoom room = chatRoomQueryService.getParticipatingRoom(roomId, role, profileId);
        ChatMessage message = chatMessageCommandService.saveText(room, role, content);
        room.onNewMessage(preview(content), sentAt(message), role);
        ChatMessageResponse response = ChatMessageResponse.from(message);
        chatEventPublisher.sendToRoom(roomId, response);
        return response;
    }

    /** 이미지 전송(한 장). CDN URL 검증 → 저장 → 방 미리보기("[사진]")·안읽음 갱신 → 브로드캐스트. 텍스트와 별개 메시지. */
    @Transactional
    public ChatMessageResponse sendImage(MemberRole role, Long profileId, Long roomId, String imageUrl) {
        fileUrlValidator.validate(imageUrl);
        ChatRoom room = chatRoomQueryService.getParticipatingRoom(roomId, role, profileId);
        ChatMessage message = chatMessageCommandService.saveImage(room, role, imageUrl);
        room.onNewMessage(IMAGE_PREVIEW, sentAt(message), role);
        ChatMessageResponse response = ChatMessageResponse.from(message);
        chatEventPublisher.sendToRoom(roomId, response);
        return response;
    }

    /** 읽음 처리. 내 쪽 안읽음 0 + 마지막 읽은 시각 갱신. */
    @Transactional
    public void markRead(MemberRole role, Long profileId, Long roomId) {
        ChatRoom room = chatRoomQueryService.getParticipatingRoom(roomId, role, profileId);
        room.markRead(role, LocalDateTime.now());
    }

    /**
     * 문의 생성 시 호출(InquiryCreatedEvent 리스너). 방 find-or-create → 문의 카드 append(발신=고객)
     * → 방 미리보기·안읽음 갱신 → 작가에게 브로드캐스트. 문의 트랜잭션 안에서 동기 실행된다.
     *
     * @return 문의가 붙은 채팅방 ID (기존 방이 있으면 그 방)
     */
    @Transactional
    public Long onInquiryCreated(Inquiry inquiry) {
        Customer customer = inquiry.getCustomer();
        Artist artist = inquiry.getArtworkPackage().getArtwork().getArtist();
        ChatRoom room = chatRoomCommandService.findOrCreate(customer, artist);
        ChatMessage message = chatMessageCommandService.saveInquiryCard(room, MemberRole.CUSTOMER, inquiry);
        room.onNewMessage("[문의] " + inquiry.getPackageNameSnapshot(), sentAt(message), MemberRole.CUSTOMER);
        chatEventPublisher.sendToRoom(room.getChatRoomId(), ChatMessageResponse.from(message));
        return room.getChatRoomId();
    }

    private static LocalDateTime sentAt(ChatMessage message) {
        return message.getCreatedAt() != null ? message.getCreatedAt() : LocalDateTime.now();
    }

    private static String preview(String content) {
        String trimmed = content.strip();
        return trimmed.length() <= PREVIEW_MAX ? trimmed : trimmed.substring(0, PREVIEW_MAX);
    }
}
