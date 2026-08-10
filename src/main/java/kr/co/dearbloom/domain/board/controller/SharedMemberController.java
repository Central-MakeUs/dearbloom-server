package kr.co.dearbloom.domain.board.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.dearbloom.domain.board.dto.board.response.SharedBoardInviteCodeResponse;
import kr.co.dearbloom.domain.board.dto.board.response.SharedBoardInviteResponse;
import kr.co.dearbloom.domain.board.dto.board.response.SharedBoardJoinResponse;
import kr.co.dearbloom.domain.board.dto.board.response.SharedMemberListResponse;
import kr.co.dearbloom.domain.board.facade.SharedMemberFacade;
import kr.co.dearbloom.domain.customer.entity.Customer;
import kr.co.dearbloom.global.auth.resolver.CurrentCustomer;
import kr.co.dearbloom.global.auth.resolver.CurrentViewer;
import kr.co.dearbloom.global.auth.resolver.ViewerContext;
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
@Tag(name = "Shared Member", description = "공동보드 초대 및 멤버 API")
public class SharedMemberController {
    private final SharedMemberFacade sharedMemberFacade;

    @GetMapping("/invite/{inviteCode}")
    @Operation(summary = "초대 링크 미리보기 조회",
            description = """
                    초대 링크로 들어온 화면에 보여줄 공동보드 정보를 조회합니다. <b>로그인하지 않아도 호출할 수 있습니다.</b><br>
                    보드명 / 방장 이름 / 현재 인원과, 이 링크를 연 사람이 <b>이미 참여 중인지</b>(alreadyJoined)를 돌려줍니다.<br>
                    프론트는 이 값으로 화면을 나눕니다 — 비로그인이면 로그인 유도, 미참여면 참여 버튼,
                    이미 참여 중이면 곧바로 보드로 이동.<br>
                    코드만 알면 볼 수 있는 응답이므로 <b>작품·댓글·멤버 목록은 포함하지 않습니다.</b><br>
                    유효하지 않은 코드면 404 를 반환합니다.
                    """)
    @ApiErrorCodes({ErrorCode.SHARED_BOARD_INVITE_CODE_INVALID})
    public ResponseEntity<ApiResponse<SharedBoardInviteResponse>> getInvite(
            @CurrentViewer ViewerContext viewer,
            @PathVariable String inviteCode
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                sharedMemberFacade.getInvite(viewer, inviteCode)
        ));
    }

    @PostMapping("/invite/{inviteCode}/members")
    @Operation(summary = "초대 링크로 공동보드 입장",
            description = """
                    초대 링크의 코드로 공동보드에 입장해 참여자(공유멤버)가 됩니다.<br>
                    <b>이미 참여 중이어도 성공(201)</b> 으로 응답합니다 — 카톡에 남은 링크를 다시 눌러도
                    에러 없이 그대로 보드로 들어갈 수 있어야 하기 때문입니다.<br>
                    유효하지 않은 코드면 404 를 반환합니다.<br>
                    응답으로 참여자 고객 ID 와 입장한 보드 정보를 돌려줍니다.
                    """)
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.ROLE_ACCESS_DENIED,
            ErrorCode.CUSTOMER_NOT_FOUND, ErrorCode.SHARED_BOARD_INVITE_CODE_INVALID})
    public ResponseEntity<ApiResponse<SharedBoardJoinResponse>> joinByInviteCode(
            @CurrentCustomer Customer customer,
            @PathVariable String inviteCode
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                sharedMemberFacade.joinByInviteCode(customer, inviteCode)
        ));
    }

    @GetMapping("/{sharedBoardId}/invite-code")
    @Operation(summary = "초대 코드 조회 (공유하기)",
            description = """
                    공유하기 화면에서 쓸 초대 코드를 조회합니다. 프론트는 이 코드로 공유 링크를 조립합니다.<br>
                    <b>참여 중인 멤버만</b> 조회할 수 있으며(방장뿐 아니라 모든 멤버가 초대 가능),
                    참여자가 아니면 403 을 반환합니다.<br>
                    코드가 곧 입장 권한이므로 보드 목록·생성 등 다른 응답에는 포함되지 않습니다.
                    """)
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.ROLE_ACCESS_DENIED,
            ErrorCode.CUSTOMER_NOT_FOUND, ErrorCode.SHARED_BOARD_NOT_FOUND,
            ErrorCode.SHARED_MEMBER_NOT_JOINED})
    public ResponseEntity<ApiResponse<SharedBoardInviteCodeResponse>> getInviteCode(
            @CurrentCustomer Customer customer,
            @PathVariable Long sharedBoardId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                sharedMemberFacade.getInviteCode(customer, sharedBoardId)
        ));
    }

    @GetMapping("/{sharedBoardId}/members")
    @Operation(summary = "공동보드 멤버 조회",
            description = """
                    공동보드의 참여자(공유멤버) 목록과 인원을 입장 순으로 조회합니다. <b>방장도 공동보드 멤버로 포함</b>됩니다.<br>
                    각 항목은 고객 ID / 이름 / 기본 프로필 이미지 색상입니다.<br>
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
