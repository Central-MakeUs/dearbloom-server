//package kr.co.dearbloom.global.s3;
//
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/s3")
//@RequiredArgsConstructor
//@Tag(name = "S3", description = "이미지 업로드/삭제 API")
//public class S3ImageController {
//
//    private final S3ImageService s3ImageService;
//
//    @PostMapping("/presigned-url")
//    @Operation(
//            summary = "업로드용 Presigned URL 발급",
//            description = """
//                    S3 업로드를 위한 Presigned URL을 발급합니다.
//
//                    **프론트엔드 사용 순서:**
//                    1. 이 API로 `presignedUrl`과 `imageUrl`을 받습니다.
//                    2. `presignedUrl`로 PUT 요청하여 파일을 직접 S3에 업로드합니다.
//                    3. `imageUrl`을 서버에 저장합니다.
//
//                    **prefix 예시:** `images`, `spots`, `profiles`
//                    """
//    )
//    public ResponseEntity<ApiResponse<PresignedUrlResponse>> getPresignedUrl(
//            @RequestBody PresignedUrlRequest request
//    ) {
//        PresignedUrlResponse response = s3ImageService.getUploadPresignedUrl(request.getPrefix(), request.getFileName());
//        return ResponseEntity.ok(ApiResponse.success(response));
//    }
//
//    @DeleteMapping
//    @Operation(summary = "이미지 삭제", description = "imageUrl로 S3에서 이미지를 삭제합니다.")
//    public ResponseEntity<ApiResponse<Void>> delete(@RequestParam String imageUrl) {
//        S3ImageUrlValidator.validate(imageUrl);
//        s3ImageService.delete(imageUrl);
//        return ResponseEntity.ok(ApiResponse.success());
//    }
//}
