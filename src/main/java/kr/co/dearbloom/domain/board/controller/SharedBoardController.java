package kr.co.dearbloom.domain.board.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.dearbloom.domain.board.dto.board.request.SharedBoardCreateRequest;
import kr.co.dearbloom.domain.board.dto.board.request.SharedBoardNameUpdateRequest;
import kr.co.dearbloom.domain.board.dto.board.response.SharedBoardResponse;
import kr.co.dearbloom.domain.board.dto.board.response.SharedBoardSummaryResponse;
import kr.co.dearbloom.domain.board.facade.SharedBoardFacade;
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
@Tag(name = "Shared Board", description = "공동보드 관리 API")
public class SharedBoardController {
    private final SharedBoardFacade sharedBoardFacade;

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
                    보드에 딸린 <b>댓글 / 공유작품 좋아요 / 공유작품 / 공유멤버</b>가 함께 삭제됩니다
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
}
