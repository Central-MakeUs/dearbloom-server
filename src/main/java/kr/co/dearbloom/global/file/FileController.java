package kr.co.dearbloom.global.file;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.dearbloom.global.dto.response.ApiResponse;
import kr.co.dearbloom.global.file.dto.PresignedUrlRequest;
import kr.co.dearbloom.global.file.dto.PresignedUrlResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "File", description = "파일 업로드/삭제 API")
public class FileController {

    private final FileService fileService;
    private final FileUrlValidator fileUrlValidator;

    @PostMapping("/presigned-url")
    @Operation(
            summary = "업로드용 Presigned URL 발급",
            description = """
                    S3 저장소에 파일 업로드를 위한 Presigned URL을 발급합니다.

                    **프론트엔드 사용 순서:**
                    1. 이 API로 업로드할 파일 종류(prefix)를 지정해 `presignedUrl`과 `fileUrl`을 받습니다.
                    2. `presignedUrl`로 S3에 PUT 요청하여 파일을 직접 업로드합니다.
                    3. 업로드 성공 후, 받아둔 `fileUrl`을 해당 종류의 등록 API
                       (리뷰 이미지 / 작품 이미지 / 작가 프로필 이미지)에 함께 전송합니다.

                    **prefix:** REVIEW(리뷰 파일) / PORTFOLIO(작품 파일) / ARTIST_PROFILE(작가 프로필 이미지) 중 하나
                    """
    )
    public ResponseEntity<ApiResponse<PresignedUrlResponse>> getPresignedUrl(
            @RequestBody PresignedUrlRequest request
    ) {
        PresignedUrlResponse response = fileService.getUploadPresignedUrl(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping
    @Operation(summary = "[개발용] 파일 삭제", description = "fileUrl로 S3 저장소에서 파일을 직접 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> delete(@RequestParam String fileUrl) {
        fileUrlValidator.validate(fileUrl);
        fileService.delete(fileUrl);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
