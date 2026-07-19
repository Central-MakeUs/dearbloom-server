package kr.co.dearbloom.domain.artwork.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ArtworkPhotoUpdateRequest {
    @NotEmpty(message = "사진을 1장 이상 등록해주세요")
    @Valid
    @Schema(description = "교체할 사진 전체 목록. 유지할 기존 사진은 그 CDN URL 을 그대로 다시 포함해서 보내면 됩니다. "
            + "등록 순서대로 정렬됩니다.")
    private List<ArtworkPhotoRequest> photoList;
}
