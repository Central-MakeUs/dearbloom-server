package kr.co.dearbloom.domain.customer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/** 저장 다중 삭제. 삭제할 작품 ID 목록. */
@Getter
@NoArgsConstructor
public class SavedArtworkDeleteRequest {
    @NotEmpty(message = "삭제할 작품 ID를 1개 이상 보내주세요")
    @Schema(description = "저장 취소할 작품 ID 목록", example = "[1, 2, 3]")
    private List<Long> artworkIdList;
}
