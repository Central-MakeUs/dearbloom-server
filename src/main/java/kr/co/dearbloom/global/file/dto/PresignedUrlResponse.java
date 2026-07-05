package kr.co.dearbloom.global.file.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "Presigned URL 응답 DTO")
public class PresignedUrlResponse {

    @Schema(description = "업로드용 Presigned URL (PUT 요청에 사용)", example = "https://dearbloom-bucket.s3.ap-northeast-2.amazonaws.com/images/...")
    private final String presignedUrl;

    @Schema(description = "업로드 후 서버에 저장할 실제 파일 접근 URL", example = "https://dearbloom-bucket.s3.ap-northeast-2.amazonaws.com/images/...")
    private final String fileUrl;
}
