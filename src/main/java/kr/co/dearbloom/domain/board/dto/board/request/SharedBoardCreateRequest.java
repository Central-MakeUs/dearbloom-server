package kr.co.dearbloom.domain.board.dto.board.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 공동보드 생성. 생성한 고객이 방장이 된다. */
@Getter
@NoArgsConstructor
public class SharedBoardCreateRequest {
    @NotBlank
    @Size(min = 2, max = 12)
    @Schema(description = "보드 이름 (2~12자)", example = "우리 졸업스냅 모음")
    private String boardName;
}
