package kr.co.dearbloom.global.file;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.dearbloom.global.dto.response.ApiResponse;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import kr.co.dearbloom.global.file.dto.PresignedUrlRequest;
import kr.co.dearbloom.global.file.dto.PresignedUrlResponse;
import kr.co.dearbloom.global.swagger.ApiErrorCodes;
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

                    **prefix:** REVIEW(리뷰 파일) / PORTFOLIO(작품 파일) / ARTIST_IMAGE(작가 대표 이미지) 중 하나
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Presigned URL 발급 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "잘못된 요청 (prefix가 REVIEW/PORTFOLIO/ARTIST_IMAGE 중 하나가 아니거나 fileName 누락)")
    })
    public ResponseEntity<ApiResponse<PresignedUrlResponse>> getPresignedUrl(
            @RequestBody PresignedUrlRequest request
    ) {
        PresignedUrlResponse response = fileService.getUploadPresignedUrl(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping
    @Operation(summary = "[개발용] 파일 삭제", description = "fileUrl로 S3 저장소에서 파일을 직접 삭제합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "잘못된 fileUrl (허용된 저장소 경로가 아님)")
    })
    @ApiErrorCodes(ErrorCode.INVALID_FILE_URL)
    public ResponseEntity<ApiResponse<Void>> delete(@RequestParam String fileUrl) {
        fileUrlValidator.validate(fileUrl);
        fileService.delete(fileUrl);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
