package kr.co.dearbloom.domain.artist.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import kr.co.dearbloom.domain.artist.entity.Region;
import kr.co.dearbloom.global.validation.validatator.ValidNickname;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@Getter
@NoArgsConstructor
public class ArtistCreateRequest {
    @NotBlank
    @ValidNickname
    @Schema(description = "닉네임 (2-12자의 한글, 영문, 숫자, _)", example = "블룸작가")
    private String nickname;

    @Schema(description = "대표 이미지 CDN URL (presigned 업로드로 받은 fileUrl). 선택 항목이며, "
            + "보내지 않거나 null 이면 이미지 없이 생성됩니다.",
            example = "https://cdn.dearbloom.co.kr/artist/uuid.webp")
    private String imageUrl;

    @NotEmpty(message = "활동 지역을 1개 이상 선택해주세요")
    @Schema(description = "활동 지역(다중 선택). 최소 1개 이상 필수입니다.",
            example = "[\"SEOUL\", \"GYEONGGI_NORTH\"]")
    private Set<Region> regionList;
}
