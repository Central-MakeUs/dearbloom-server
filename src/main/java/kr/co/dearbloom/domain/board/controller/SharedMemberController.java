package kr.co.dearbloom.domain.board.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.dearbloom.domain.board.dto.board.response.SharedBoardJoinResponse;
import kr.co.dearbloom.domain.board.dto.board.response.SharedMemberListResponse;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shared-boards")
@Tag(name = "Shared Member", description = "공동보드 멤버 API")
public class SharedMemberController {
    private final SharedMemberFacade sharedMemberFacade;

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
                    공동보드의 참여자(공유멤버) 목록과 인원을 입장 순으로 조회합니다. <b>방장도 공동보드 멤버로 포함</b>됩니다.<br>
                    각 항목은 공유멤버 ID / 고객 이름입니다.<br>
                    보드 내부 정보이므로 <b>공동보드 멤버만</b> 조회할 수 있으며, 공유 멤버가 아니면 403 을 반환합니다.
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
                    공동보드에서 나갑니다. <b>일반 멤버만 탈퇴</b>할 수 있습니다.<br>
                    <b>방장은 나갈 수 없고</b>(403) 보드 삭제 API 만 사용할 수 있습니다.
                    공유 멤버가 아니면 403 을 반환합니다.<br>
                    탈퇴하면 내가 이 보드에 남긴 <b>댓글 / 공유작품 좋아요 / 공유작품</b>이 함께 삭제됩니다.
                    내가 담은 공유작품에 다른 멤버가 누른 좋아요도 함께 사라지며,
                    원본 작품과 다른 멤버가 담은 공유작품·다른 멤버의 댓글은 그대로 남습니다.
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
}
