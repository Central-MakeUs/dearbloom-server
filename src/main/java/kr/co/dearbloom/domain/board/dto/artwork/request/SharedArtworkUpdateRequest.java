package kr.co.dearbloom.domain.board.dto.artwork.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/** 내가 이 보드에 공유할 작품 전체 목록(부분 수정 아님). 보낸 목록이 그대로 내 공유작품이 된다. */
@Getter
@NoArgsConstructor
public class SharedArtworkUpdateRequest {
    @NotNull
    @Size(max = 3)
    @Schema(description = "공유할 작품 ID 목록 (최대 3개). 빈 배열이면 내 공유작품을 모두 제거합니다.",
            example = "[1, 5, 9]")
    private List<Long> artworkIdList;
}
