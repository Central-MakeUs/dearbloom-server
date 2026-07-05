package kr.co.dearbloom.global.file.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "Presigned URL 요청 DTO")
public class PresignedUrlRequest {

    @Schema(description = "저장 폴더 (예: images, works, profiles)", example = "images")
    private final String prefix;

    @Schema(description = "원본 파일명", example = "photo.jpg")
    private final String fileName;
}
