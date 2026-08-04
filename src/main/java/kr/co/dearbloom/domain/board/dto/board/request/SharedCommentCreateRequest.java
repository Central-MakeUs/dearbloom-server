package kr.co.dearbloom.domain.board.dto.board.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 공동보드 댓글 등록. 개별 작품이 아니라 보드에 남기는 댓글이다. */
@Getter
@NoArgsConstructor
public class SharedCommentCreateRequest {
    @NotBlank
    @Size(max = 500)
    @Schema(description = "댓글 내용 (500자 이내)", example = "이 컨셉 너무 좋다! 우리 이걸로 갈까?")
    private String content;
}
