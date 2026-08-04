package kr.co.dearbloom.domain.board.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.dearbloom.domain.board.dto.board.request.SharedBoardCreateRequest;
import kr.co.dearbloom.domain.board.dto.board.request.SharedBoardNameUpdateRequest;
import kr.co.dearbloom.domain.board.dto.board.response.SharedBoardJoinResponse;
import kr.co.dearbloom.domain.board.dto.board.response.SharedBoardResponse;
import kr.co.dearbloom.domain.board.dto.board.response.SharedBoardSummaryResponse;
import kr.co.dearbloom.domain.board.dto.board.response.SharedMemberListResponse;
import kr.co.dearbloom.domain.board.facade.SharedBoardFacade;
import kr.co.dearbloom.domain.board.facade.SharedMemberFacade;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shared-boards")
@Tag(name = "Shared Board", description = "공동보드 API")
public class SharedBoardController {
    private final SharedBoardFacade sharedBoardFacade;
    private final SharedMemberFacade sharedMemberFacade;

    // ──────────────────────── 공동보드 API ────────────────────────

    @GetMapping
    @Operation(summary = "내가 속한 공동보드 목록 조회",
            description = """
                    내가 참여 중인(방장이거나 입장한) 공동보드를 <b>보드 생성 오름차순</b>으로 조회합니다.<br>
                    각 카드는 보드명 / 보드에 담긴 작품 개수 / 미리보기 이미지로 구성됩니다.<br>
                    미리보기 이미지는 보드에 담긴 <b>각 작품의 첫 번째 사진</b>이며 <b>최대 4장</b>입니다
                    (작품이 1개면 1장, 2개면 2장, 3개면 3장, 4개 이상이면 4장). 담긴 작품이 없으면 빈 배열입니다.
                    """)
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.ROLE_ACCESS_DENIED,
            ErrorCode.CUSTOMER_NOT_FOUND})
    public ResponseEntity<ApiResponse<List<SharedBoardSummaryResponse>>> getJoinedBoards(
            @CurrentCustomer Customer customer
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                sharedBoardFacade.getJoinedBoards(customer)
        ));
    }

    @PostMapping
    @Operation(summary = "공동보드 생성",
            description = """
                    공동보드를 생성합니다. 보드 이름은 <b>2~12자</b>입니다.<br>
                    <b>생성한 사람이 방장(ownerId)</b>이 되며, 방장도 참여자로 함께 등록됩니다
                    (별도로 입장 API 를 호출할 필요 없음).
                    """)
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.ROLE_ACCESS_DENIED,
            ErrorCode.CUSTOMER_NOT_FOUND, ErrorCode.PARAMETER_BAD_REQUEST})
    public ResponseEntity<ApiResponse<SharedBoardResponse>> create(
            @CurrentCustomer Customer customer,
            @RequestBody @Valid SharedBoardCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                sharedBoardFacade.create(customer, request)
        ));
    }

    @PatchMapping("/{sharedBoardId}")
    @Operation(summary = "공동보드 이름 수정",
            description = """
                    공동보드의 이름을 수정합니다. 보드 이름은 <b>2~12자</b>입니다.<br>
                    <b>방장만 수정</b>할 수 있으며, 방장이 아니면 403 을 반환합니다.
                    """)
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.ROLE_ACCESS_DENIED,
            ErrorCode.CUSTOMER_NOT_FOUND, ErrorCode.PARAMETER_BAD_REQUEST,
            ErrorCode.SHARED_BOARD_NOT_FOUND, ErrorCode.SHARED_BOARD_ACCESS_DENIED})
    public ResponseEntity<ApiResponse<SharedBoardResponse>> updateBoardName(
            @CurrentCustomer Customer customer,
            @PathVariable Long sharedBoardId,
            @RequestBody @Valid SharedBoardNameUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                sharedBoardFacade.updateBoardName(customer, sharedBoardId, request)
        ));
    }

    @DeleteMapping("/{sharedBoardId}")
    @Operation(summary = "공동보드 삭제",
            description = """
                    공동보드를 삭제합니다. <b>방장만 삭제</b>할 수 있으며, 방장이 아니면 403 을 반환합니다.<br>
                    보드에 딸린 <b>공유작품 댓글 / 공유작품 좋아요 / 공유작품 / 공유멤버</b>가 함께 삭제됩니다
                    (원본 작품은 삭제되지 않습니다).<br>
                    응답은 삭제된 보드의 정보입니다.
                    """)
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.ROLE_ACCESS_DENIED,
            ErrorCode.CUSTOMER_NOT_FOUND, ErrorCode.SHARED_BOARD_NOT_FOUND,
            ErrorCode.SHARED_BOARD_ACCESS_DENIED})
    public ResponseEntity<ApiResponse<SharedBoardResponse>> delete(
            @CurrentCustomer Customer customer,
            @PathVariable Long sharedBoardId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                sharedBoardFacade.delete(customer, sharedBoardId)
        ));
    }

    // ──────────────────────── 공유 멤버 API ────────────────────────

    @PostMapping("/{sharedBoardId}/members")
    @Operation(summary = "공동보드 멤버로 입장하기",
            description = """
                    초대받은 공동보드에 입장해 참여자(공유멤버)가 됩니다.<br>
                    <b>이미 참여 중인 보드면 409</b> 를 반환합니다(방장은 생성 시점에 이미 참여자이므로 409).<br>
                    응답으로 생성된 공유멤버 ID 와 입장한 보드 정보를 돌려줍니다.
                    """)
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.ROLE_ACCESS_DENIED,
            ErrorCode.CUSTOMER_NOT_FOUND, ErrorCode.SHARED_BOARD_NOT_FOUND,
            ErrorCode.SHARED_MEMBER_ALREADY_JOINED})
    public ResponseEntity<ApiResponse<SharedBoardJoinResponse>> join(
            @CurrentCustomer Customer customer,
            @PathVariable Long sharedBoardId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                sharedMemberFacade.join(customer, sharedBoardId)
        ));
    }

    @GetMapping("/{sharedBoardId}/members")
    @Operation(summary = "공동보드 멤버 조회",
            description = """
                    공동보드의 참여자(공유멤버) 목록과 인원을 입장 순으로 조회합니다. <b>방장도 참여자로 포함</b>됩니다.<br>
                    각 항목은 공유멤버 ID / 고객 이름입니다.<br>
                    보드 내부 정보이므로 <b>참여 중인 고객만</b> 조회할 수 있으며, 참여자가 아니면 403 을 반환합니다.
                    """)
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.ROLE_ACCESS_DENIED,
            ErrorCode.CUSTOMER_NOT_FOUND, ErrorCode.SHARED_BOARD_NOT_FOUND,
            ErrorCode.SHARED_MEMBER_NOT_JOINED})
    public ResponseEntity<ApiResponse<SharedMemberListResponse>> getMembers(
            @CurrentCustomer Customer customer,
            @PathVariable Long sharedBoardId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                sharedMemberFacade.getMembers(customer, sharedBoardId)
        ));
    }

    @DeleteMapping("/{sharedBoardId}/members/me")
    @Operation(summary = "공동보드 멤버에서 탈퇴",
            description = """
                    공동보드에서 나갑니다. <b>일반 참여자만 탈퇴</b>할 수 있습니다.<br>
                    <b>방장은 나갈 수 없고</b>(403) 보드 삭제 API 만 사용할 수 있습니다.
                    참여자가 아니면 403 을 반환합니다.<br>
                    탈퇴하면 내가 이 보드에 남긴 <b>공유작품 / 공유작품 좋아요 / 공유작품 댓글</b>이 함께 삭제됩니다.
                    내가 담은 공유작품에 다른 참여자가 남긴 좋아요·댓글도 함께 사라지며,
                    원본 작품과 다른 참여자가 담은 공유작품은 그대로 남습니다.
                    """)
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.ROLE_ACCESS_DENIED,
            ErrorCode.CUSTOMER_NOT_FOUND, ErrorCode.SHARED_BOARD_NOT_FOUND,
            ErrorCode.SHARED_BOARD_OWNER_CANNOT_LEAVE, ErrorCode.SHARED_MEMBER_NOT_JOINED})
    public ResponseEntity<ApiResponse<Void>> leave(
            @CurrentCustomer Customer customer,
            @PathVariable Long sharedBoardId
    ) {
        sharedMemberFacade.leave(customer, sharedBoardId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // ──────────────────────── 공동보드 댓글 API ────────────────────────
}
