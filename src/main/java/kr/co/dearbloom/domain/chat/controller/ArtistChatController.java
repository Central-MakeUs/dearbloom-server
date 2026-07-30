package kr.co.dearbloom.domain.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.dearbloom.domain.artist.entity.artist.Artist;
import kr.co.dearbloom.domain.chat.dto.request.ChatImageSendRequest;
import kr.co.dearbloom.domain.chat.dto.request.ChatMessageSendRequest;
import kr.co.dearbloom.domain.chat.dto.response.ChatMessageResponse;
import kr.co.dearbloom.domain.chat.dto.response.artist.ArtistChatMessageResponse;
import kr.co.dearbloom.domain.chat.dto.response.artist.ArtistChatRoomSummaryResponse;
import kr.co.dearbloom.domain.chat.facade.ChatFacade;
import kr.co.dearbloom.domain.member.entity.MemberRole;
import kr.co.dearbloom.global.auth.resolver.CurrentArtist;
import kr.co.dearbloom.global.dto.response.ApiResponse;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import kr.co.dearbloom.global.swagger.ApiErrorCodes;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/artists/me/chat")
@RequiredArgsConstructor
@Tag(name = "Chat - Artist", description = "작가 채팅 API")
public class ArtistChatController {
    private final ChatFacade chatFacade;

    @GetMapping("/rooms")
    @Operation(summary = "채팅방 목록 조회",
            description = "내 채팅방을 최근 메시지순으로 반환합니다. "
                    + "각 방은 고객명, 마지막 메시지 미리보기·시각, 안읽음 수를 포함합니다.")
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.ROLE_ACCESS_DENIED,
            ErrorCode.ARTIST_NOT_FOUND})
    public ResponseEntity<ApiResponse<List<ArtistChatRoomSummaryResponse>>> getMyRooms(
            @CurrentArtist Artist artist
    ) {
        return ResponseEntity.ok(ApiResponse.success(chatFacade.getArtistRooms(artist.getArtistId())));
    }

    @GetMapping("/rooms/{roomId}/messages")
    @Operation(summary = "메시지 히스토리 조회",
            description = "방 메시지를 최신부터 size 개 조회해 화면 표시용(오래된→최신) 순서로 반환합니다. "
                    + "무한 스크롤은 받은 첫 메시지의 messageId 를 cursor 로 넘겨 이어 조회합니다. "
                    + "INQUIRY 메시지는 inquiryCard 로, TEXT 는 content 로 내려갑니다.")
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.ROLE_ACCESS_DENIED,
            ErrorCode.ARTIST_NOT_FOUND, ErrorCode.CHAT_ROOM_NOT_FOUND, ErrorCode.CHAT_ACCESS_DENIED})
    public ResponseEntity<ApiResponse<List<ArtistChatMessageResponse>>> getMessages(
            @CurrentArtist Artist artist,
            @PathVariable Long roomId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(required = false) Integer size
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                chatFacade.getArtistMessages(artist.getArtistId(), roomId, cursor, size)));
    }

    @PostMapping("/rooms/{roomId}/messages")
    @Operation(summary = "텍스트 메시지 전송",
            description = "방에 텍스트 메시지를 전송합니다. 저장 후 구독자에게 실시간(WebSocket /topic/rooms/{roomId})으로 "
                    + "브로드캐스트되며, 응답으로 저장된 메시지를 돌려줍니다.")
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.ROLE_ACCESS_DENIED,
            ErrorCode.ARTIST_NOT_FOUND, ErrorCode.CHAT_ROOM_NOT_FOUND, ErrorCode.CHAT_ACCESS_DENIED})
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMessage(
            @CurrentArtist Artist artist,
            @PathVariable Long roomId,
            @RequestBody @Valid ChatMessageSendRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(chatFacade.sendText(
                MemberRole.ARTIST, artist.getArtistId(), roomId, request.getContent())));
    }

    @PostMapping("/rooms/{roomId}/images")
    @Operation(summary = "이미지 메시지 전송",
            description = "presigned(prefix=CHAT_IMAGE)로 S3 업로드 후 받은 CDN URL 을 보냅니다. "
                    + "<b>한 번에 한 장</b>, 텍스트와 동시 전송 불가(엔드포인트 분리). 저장 후 텍스트와 동일하게 "
                    + "실시간 브로드캐스트되며 응답으로 저장된 메시지를 돌려줍니다.")
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.ROLE_ACCESS_DENIED,
            ErrorCode.ARTIST_NOT_FOUND, ErrorCode.CHAT_ROOM_NOT_FOUND, ErrorCode.CHAT_ACCESS_DENIED,
            ErrorCode.INVALID_FILE_URL})
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendImage(
            @CurrentArtist Artist artist,
            @PathVariable Long roomId,
            @RequestBody @Valid ChatImageSendRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(chatFacade.sendImage(
                MemberRole.ARTIST, artist.getArtistId(), roomId, request.getImageUrl())));
    }

    @PostMapping("/rooms/{roomId}/read")
    @Operation(summary = "읽음 처리",
            description = "해당 방을 읽음 처리합니다(내 쪽 안읽음 0). 보통 방 진입 시 호출합니다.")
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.ROLE_ACCESS_DENIED,
            ErrorCode.ARTIST_NOT_FOUND, ErrorCode.CHAT_ROOM_NOT_FOUND, ErrorCode.CHAT_ACCESS_DENIED})
    public ResponseEntity<ApiResponse<Void>> markRead(
            @CurrentArtist Artist artist,
            @PathVariable Long roomId
    ) {
        chatFacade.markRead(MemberRole.ARTIST, artist.getArtistId(), roomId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
