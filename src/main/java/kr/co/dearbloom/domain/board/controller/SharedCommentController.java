package kr.co.dearbloom.domain.board.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.dearbloom.domain.board.dto.board.request.SharedCommentCreateRequest;
import kr.co.dearbloom.domain.board.dto.board.response.SharedCommentResponse;
import kr.co.dearbloom.domain.board.facade.SharedCommentFacade;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 공동보드 댓글 조회·등록·삭제. 댓글은 개별 공유작품이 아니라 보드 단위로 달린다. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shared-boards")
@Tag(name = "Shared Comment", description = "공동보드 댓글 API")
public class SharedCommentController {
    private final SharedCommentFacade sharedCommentFacade;

    @GetMapping("/{sharedBoardId}/comments")
    @Operation(summary = "공동보드 댓글 조회",
            description = """
                    공동보드에 달린 댓글을 <b>작성 순(오래된 것부터)</b>으로 조회합니다.<br>
                    댓글은 개별 공유작품이 아니라 <b>보드 단위</b>로 달립니다.<br>
                    각 항목은 댓글 ID / 작성자 이름 / 내용 / 작성 시각입니다(댓글 ID 는 삭제 API 에 사용).<br>
                    보드 내부 정보이므로 <b>공동보드 멤버만</b> 조회할 수 있으며, 공유 멤버가 아니면 403 을 반환합니다.
                    """)
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.ROLE_ACCESS_DENIED,
            ErrorCode.CUSTOMER_NOT_FOUND, ErrorCode.SHARED_BOARD_NOT_FOUND,
            ErrorCode.SHARED_MEMBER_NOT_JOINED})
    public ResponseEntity<ApiResponse<List<SharedCommentResponse>>> getComments(
            @CurrentCustomer Customer customer,
            @PathVariable Long sharedBoardId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                sharedCommentFacade.getComments(customer, sharedBoardId)
        ));
    }

    @PostMapping("/{sharedBoardId}/comments")
    @Operation(summary = "공동보드 댓글 등록",
            description = """
                    공동보드에 댓글을 남깁니다. 내용은 <b>500자 이내</b>입니다.<br>
                    <b>공동보드 멤버만</b> 조회할 수 있으며, 공유 멤버가 아니면 403 을 반환합니다.
                    """)
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.ROLE_ACCESS_DENIED,
            ErrorCode.CUSTOMER_NOT_FOUND, ErrorCode.PARAMETER_BAD_REQUEST,
            ErrorCode.SHARED_BOARD_NOT_FOUND, ErrorCode.SHARED_MEMBER_NOT_JOINED})
    public ResponseEntity<ApiResponse<Void>> createComment(
            @CurrentCustomer Customer customer,
            @PathVariable Long sharedBoardId,
            @RequestBody @Valid SharedCommentCreateRequest request
    ) {
        sharedCommentFacade.create(customer, sharedBoardId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success());
    }

    @DeleteMapping("/comments/{sharedCommentId}")
    @Operation(summary = "공동보드 댓글 삭제",
            description = """
                    공동보드 댓글을 삭제합니다. <b>본인이 작성한 댓글만</b> 삭제할 수 있으며,
                    남의 댓글이면 403 을 반환합니다.
                    """)
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.ROLE_ACCESS_DENIED,
            ErrorCode.CUSTOMER_NOT_FOUND, ErrorCode.SHARED_COMMENT_NOT_FOUND,
            ErrorCode.SHARED_COMMENT_ACCESS_DENIED})
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @CurrentCustomer Customer customer,
            @PathVariable Long sharedCommentId
    ) {
        sharedCommentFacade.delete(customer, sharedCommentId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
