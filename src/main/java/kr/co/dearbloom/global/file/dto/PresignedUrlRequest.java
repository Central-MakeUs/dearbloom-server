package kr.co.dearbloom.global.file.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Presigned URL 요청 DTO")
public record PresignedUrlRequest(
        @Schema(description = "파일 종류 (저장 폴더명)", example = "REVIEW")
        FilePrefix prefix,

        @Schema(description = "원본 파일명", example = "photo1.jpg")
        String fileName
) {
}
