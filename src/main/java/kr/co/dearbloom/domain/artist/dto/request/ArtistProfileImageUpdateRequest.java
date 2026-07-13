package kr.co.dearbloom.domain.artist.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ArtistProfileImageUpdateRequest {
    @NotBlank
    @Schema(description = "새 프로필 이미지 CDN URL (presigned 업로드로 받은 fileUrl)",
            example = "https://cdn.dearbloom.co.kr/profile/artist/uuid.webp")
    private String profileImageUrl;
}
