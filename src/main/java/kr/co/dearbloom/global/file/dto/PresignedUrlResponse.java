package kr.co.dearbloom.global.file.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Presigned URL 응답 DTO")
public record PresignedUrlResponse(

        @Schema(description = "업로드용 Presigned URL (S3, PUT 요청에 사용)",
                example = "https://{bucket}.s3.ap-northeast-2.amazonaws.com/{folder}/{timestamp}-{uuid}-photo.jpg?X-Amz-Signature=...")
        String presignedUrl,

        @Schema(description = "업로드 후 DearBloom DB에 저장할 실제 파일 접근 URL (CDN)",
                example = "https://{cdn-domain}/{folder}/{timestamp}-{uuid}-photo.jpg")
        String fileUrl
) {
}
