package kr.co.dearbloom.global.file;

import io.swagger.v3.oas.annotations.Hidden;
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
@Tag(name = "File", description = "파일(이미지/영상 등) 업로드/삭제 API")
@Hidden // ToDo: 설정 끝내고 전달
public class FileController {

    private final FileService fileService;
    private final FileUrlValidator fileUrlValidator;

    @PostMapping("/presigned-url")
    @Operation(
            summary = "업로드용 Presigned URL 발급",
            description = """
                    파일 업로드를 위한 Presigned URL을 발급합니다.

                    **프론트엔드 사용 순서:**
                    1. 이 API로 `presignedUrl`과 `fileUrl`을 받습니다.
                    2. `presignedUrl`로 PUT 요청하여 파일을 직접 업로드합니다.
                    3. `fileUrl`을 서버에 저장합니다.

                    **prefix 예시:** `images`, `works`, `profiles`
                    """
    )
    public ResponseEntity<ApiResponse<PresignedUrlResponse>> getPresignedUrl(
            @RequestBody PresignedUrlRequest request
    ) {
        PresignedUrlResponse response = fileService.getUploadPresignedUrl(request.getPrefix(), request.getFileName());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping
    @Operation(summary = "파일 삭제", description = "fileUrl로 저장소에서 파일을 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> delete(@RequestParam String fileUrl) {
        fileUrlValidator.validate(fileUrl);
        fileService.delete(fileUrl);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
