package kr.co.dearbloom.domain.board.dto.board.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.dearbloom.domain.board.entity.board.SharedComment;
import kr.co.dearbloom.domain.customer.entity.Customer;

import java.time.LocalDateTime;

/** 공동보드 댓글 한 건(작성자 이름 + 내용 + 작성 시각). */
public record SharedCommentResponse(
        @Schema(description = "공동보드 댓글 ID", example = "7")
        Long sharedCommentId,

        @Schema(description = "작성자(고객) 이름", example = "김디어")
        String sharedMemberName,

        @Schema(description = "댓글 내용", example = "이 컨셉 너무 좋다! 우리 이걸로 갈까?")
        String content,

        @Schema(description = "작성 시각", example = "2026-08-04T14:32:10")
        LocalDateTime createdAt,

        @Schema(description = "내가 쓴 댓글인지 여부. true 일 때만 삭제할 수 있습니다(삭제 버튼 노출 기준).",
                example = "false")
        Boolean isMine
) {
    public static SharedCommentResponse of(SharedComment sharedComment, Customer viewer) {
        return new SharedCommentResponse(
                sharedComment.getSharedCommentId(),
                sharedComment.getCustomer().getName(),
                sharedComment.getContent(),
                sharedComment.getCreatedAt(),
                sharedComment.isWrittenBy(viewer));
    }
}
