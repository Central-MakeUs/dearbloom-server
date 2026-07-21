package kr.co.dearbloom.domain.customer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SavedArtworkCreateRequest {
    @NotNull
    @Schema(description = "저장할 작품 ID", example = "1")
    private Long artworkId;
}
