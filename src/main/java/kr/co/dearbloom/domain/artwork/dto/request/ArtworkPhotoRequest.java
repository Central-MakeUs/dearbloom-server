package kr.co.dearbloom.domain.artwork.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kr.co.dearbloom.global.file.FileType;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 사진 1장 + 그 사진에 라벨링할 학교(선택). 작품 등록 외에도 재사용 가능. */
@Getter
@NoArgsConstructor
public class ArtworkPhotoRequest {
    @NotBlank
    @Schema(description = "사진 CDN URL (presigned 업로드로 받은 fileUrl)",
            example = "https://cdn.dearbloom.co.kr/artwork/uuid.webp")
    private String fileUrl;

    @NotNull
    @Schema(description = "파일 종류", example = "IMAGE",
            allowableValues = {"IMAGE", "VIDEO", "DOCUMENT"})
    private FileType fileType;

    @Schema(description = "이 사진에 라벨링할 학교 ID. 선택 항목이며 null 가능.", example = "80")
    private Long universityId;
}
