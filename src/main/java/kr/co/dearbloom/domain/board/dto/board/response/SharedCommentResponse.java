package kr.co.dearbloom.domain.board.dto.board.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.dearbloom.domain.board.entity.board.SharedComment;

import java.time.LocalDateTime;

/** 공동보드 댓글 한 건(작성자 이름 + 내용 + 작성 시각). */
public record SharedCommentResponse(
        @Schema(description = "공동보드 댓글 ID", example = "7")
        Long sharedCommentId,

        @Schema(description = "작성자(고객) 이름", example = "김디어")
        String customerName,

        @Schema(description = "댓글 내용", example = "이 컨셉 너무 좋다! 우리 이걸로 갈까?")
        String content,

        @Schema(description = "작성 시각", example = "2026-08-04T14:32:10")
        LocalDateTime createdAt
) {
    public static SharedCommentResponse from(SharedComment sharedComment) {
        return new SharedCommentResponse(
                sharedComment.getSharedCommentId(),
                sharedComment.getCustomer().getName(),
                sharedComment.getContent(),
                sharedComment.getCreatedAt());
    }
}
