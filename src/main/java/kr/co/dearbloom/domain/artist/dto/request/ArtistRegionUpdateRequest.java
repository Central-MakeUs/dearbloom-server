package kr.co.dearbloom.domain.artist.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import kr.co.dearbloom.domain.artist.entity.Region;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@Getter
@NoArgsConstructor
public class ArtistRegionUpdateRequest {
    @NotEmpty(message = "활동 지역을 1개 이상 선택해주세요")
    @Schema(description = "활동 지역(다중 선택). 보낸 값으로 전체 교체되며, 최소 1개 이상 필수입니다.",
            example = "[\"SEOUL\", \"GYEONGGI_NORTH\"]")
    private Set<Region> regionList;
}
