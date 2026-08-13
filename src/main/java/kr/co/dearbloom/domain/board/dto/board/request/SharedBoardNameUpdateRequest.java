package kr.co.dearbloom.domain.board.dto.board.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 공동보드 이름 수정. 방장만 가능. */
@Getter
@NoArgsConstructor
public class SharedBoardNameUpdateRequest {
    @NotBlank
    @Size(min = 2, max = 12)
    @Schema(description = "변경할 보드 이름 (2~12자)", example = "졸업스냅 최종본")
    private String sharedBoardName;
}
