package kr.co.dearbloom.domain.board.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.dearbloom.domain.board.dto.artwork.request.SharedArtworkUpdateRequest;
import kr.co.dearbloom.domain.board.dto.artwork.response.SavedArtworkIsSharedResponse;
import kr.co.dearbloom.domain.board.dto.artwork.response.SharedArtworkPageResponse;
import kr.co.dearbloom.domain.board.dto.artwork.response.SharedArtworkUpdateResponse;
import kr.co.dearbloom.domain.board.facade.SharedArtworkFacade;
import kr.co.dearbloom.domain.customer.entity.Customer;
import kr.co.dearbloom.global.auth.resolver.CurrentCustomer;
import kr.co.dearbloom.global.dto.response.ApiResponse;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import kr.co.dearbloom.global.swagger.ApiErrorCodes;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shared-boards")
@Tag(name = "Shared Artwork", description = "공동보드 공유작품 API")
public class SharedArtworkController {
    private final SharedArtworkFacade sharedArtworkFacade;

    @GetMapping("/{sharedBoardId}/artworks")
    @Operation(summary = "공동보드 공유작품 페이지 조회",
            description = """
                    공동보드 화면 한 장에 필요한 <b>참여자 목록·인원 + 공유작품 목록·개수</b>를 함께 조회합니다.<br>
                    <b>같은 작품을 여러 참여자가 담았어도 작품 하나로 합쳐서</b> 내려갑니다
                    (공유작품 ID 는 가장 먼저 담긴 행의 ID 이며, 좋아요 API 에 이 ID 를 사용합니다).<br>
                    정렬은 <b>좋아요 많은 순</b>, 좋아요가 같으면 <b>먼저 담긴 순</b>입니다.
                    좋아요 수는 같은 작품의 여러 행에 달린 것을 합산합니다.<br>
                    보드 내부 정보이므로 <b>공동보드 멤버만</b> 조회할 수 있으며, 공유 멤버가 아니면 403 을 반환합니다.
                    """)
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.ROLE_ACCESS_DENIED,
            ErrorCode.CUSTOMER_NOT_FOUND, ErrorCode.SHARED_BOARD_NOT_FOUND,
            ErrorCode.SHARED_MEMBER_NOT_JOINED})
    public ResponseEntity<ApiResponse<SharedArtworkPageResponse>> getPage(
            @CurrentCustomer Customer customer,
            @PathVariable Long sharedBoardId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                sharedArtworkFacade.getPage(customer, sharedBoardId)
        ));
    }

    @GetMapping("/{sharedBoardId}/saved-artworks")
    @Operation(summary = "내 저장 작품 조회 (공동보드 공유 여부 포함)",
            description = """
                    공유작품을 고르는 화면용입니다. 내 저장 작품 목록을 각 작품의
                    <b>이 보드 공유 여부(isShared)</b>와 함께 조회합니다.<br>
                    isShared 는 <b>내가</b> 이 보드에 공유했는지를 뜻합니다(다른 참여자가 담은 것은 false).
                    공유작품 업데이트 API 의 체크 상태로 그대로 쓰면 됩니다.<br>
                    <b>공유 중인 작품이 맨 위</b>로 올라오고, 나머지는 저장 최신순입니다.<br>
                    <b>공동보드 멤버만</b> 조회할 수 있으며, 공유 멤버가 아니면 403 을 반환합니다.
                    """)
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.ROLE_ACCESS_DENIED,
            ErrorCode.CUSTOMER_NOT_FOUND, ErrorCode.SHARED_BOARD_NOT_FOUND,
            ErrorCode.SHARED_MEMBER_NOT_JOINED})
    public ResponseEntity<ApiResponse<List<SavedArtworkIsSharedResponse>>> getSavedArtworks(
            @CurrentCustomer Customer customer,
            @PathVariable Long sharedBoardId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                sharedArtworkFacade.getSavedArtworks(customer, sharedBoardId)
        ));
    }

    @PutMapping("/{sharedBoardId}/artworks")
    @Operation(summary = "공동보드에 공유작품 추가/삭제 업데이트 (다중 선택)",
            description = """
                    이 보드에 <b>내가 최종적으로 공유하길 원하는 작품 ID 전체 목록</b>을 보내주세요(부분 수정 아님).
                    보낸 목록이 그대로 내 공유작품이 됩니다. <b>최대 3개</b>이며, 빈 배열이면 전부 내려갑니다.<br>
                    - <b>유지할 작품</b>: 기존 작품 ID 를 그대로 다시 포함해 보내면 됩니다(좋아요도 유지).<br>
                    - <b>목록에서 뺀 작품</b>: 내 공유작품에서 사라지며, 거기 달린 좋아요도 함께 삭제됩니다.<br>
                    다른 참여자가 같은 작품을 담고 있어도 그 사람 공유작품은 그대로 남습니다.<br>
                    응답은 보드 정보와 <b>업데이트 후 내 공유작품 목록</b>입니다.<br>
                    <b>공동보드 멤버만</b> 사용할 수 있으며, 공유 멤버가 아니면 403 을 반환합니다.
                    """)
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.ROLE_ACCESS_DENIED,
            ErrorCode.CUSTOMER_NOT_FOUND, ErrorCode.PARAMETER_BAD_REQUEST,
            ErrorCode.SHARED_BOARD_NOT_FOUND, ErrorCode.SHARED_MEMBER_NOT_JOINED,
            ErrorCode.ARTWORK_NOT_FOUND})
    public ResponseEntity<ApiResponse<SharedArtworkUpdateResponse>> update(
            @CurrentCustomer Customer customer,
            @PathVariable Long sharedBoardId,
            @RequestBody @Valid SharedArtworkUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                sharedArtworkFacade.update(customer, sharedBoardId, request)
        ));
    }

    // ──────────────────────── 공유작품 좋아요 API ────────────────────────

    @PostMapping("/artworks/{sharedArtworkId}/likes")
    @Operation(summary = "공유작품 좋아요 등록",
            description = """
                    공유작품에 좋아요를 누릅니다. 공유작품 ID 는 공유작품 페이지 조회 응답의 ID 를 사용하세요.<br>
                    <b>이미 좋아요한 공유작품이면 409</b> 를 반환합니다.<br>
                    <b>공동보드 멤버만</b> 누를 수 있으며, 공유 멤버가 아니면 403 을 반환합니다.
                    """)
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.ROLE_ACCESS_DENIED,
            ErrorCode.CUSTOMER_NOT_FOUND, ErrorCode.SHARED_ARTWORK_NOT_FOUND,
            ErrorCode.SHARED_MEMBER_NOT_JOINED, ErrorCode.SHARED_ARTWORK_ALREADY_LIKED})
    public ResponseEntity<ApiResponse<Void>> like(
            @CurrentCustomer Customer customer,
            @PathVariable Long sharedArtworkId
    ) {
        sharedArtworkFacade.like(customer, sharedArtworkId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success());
    }

    @DeleteMapping("/artworks/{sharedArtworkId}/likes")
    @Operation(summary = "공유작품 좋아요 삭제",
            description = """
                    공유작품 좋아요를 취소합니다. 좋아요를 누르지 않은 상태여도 정상 처리됩니다(멱등).<br>
                    <b>공동보드 멤버만</b> 사용할 수 있으며, 참여자가 아니면 403 을 반환합니다.
                    """)
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.ROLE_ACCESS_DENIED,
            ErrorCode.CUSTOMER_NOT_FOUND, ErrorCode.SHARED_ARTWORK_NOT_FOUND,
            ErrorCode.SHARED_MEMBER_NOT_JOINED})
    public ResponseEntity<ApiResponse<Void>> unlike(
            @CurrentCustomer Customer customer,
            @PathVariable Long sharedArtworkId
    ) {
        sharedArtworkFacade.unlike(customer, sharedArtworkId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
