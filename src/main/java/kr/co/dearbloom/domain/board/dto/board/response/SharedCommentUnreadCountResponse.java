package kr.co.dearbloom.domain.board.dto.board.response;

import io.swagger.v3.oas.annotations.media.Schema;

/** 댓글 안읽음 뱃지 갱신용 경량 응답. 보드 화면 전체를 다시 받지 않고 숫자만 가져올 때 쓴다. */
public record SharedCommentUnreadCountResponse(
        @Schema(description = "내가 안 읽은 댓글 수(내가 쓴 댓글 제외). 0 이면 뱃지를 숨기세요.", example = "1")
        long unreadCommentCount
) {
}
