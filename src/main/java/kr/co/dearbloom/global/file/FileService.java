package kr.co.dearbloom.global.file;

import kr.co.dearbloom.global.file.dto.PresignedUrlRequest;
import kr.co.dearbloom.global.file.dto.PresignedUrlResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

/**
 * 파일(이미지/영상 등) 업로드·삭제. AWS S3 를 직접 사용한다.
 * 업로드는 S3 presigned URL(PUT), 최종 접근 URL 은 CDN(CloudFront) 기준으로 조립한다.
 */
@Service
@RequiredArgsConstructor
public class FileService {

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${spring.cloud.aws.s3.exp-time}")
    private Long expTime;

    @Value("${file.cdn-url}")
    private String cdnUrl;

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    /** 클라이언트가 직접 업로드할 Presigned URL(S3) 과 최종 접근 URL(CDN) 발급. */
    public PresignedUrlResponse getUploadPresignedUrl(PresignedUrlRequest request) {
        String key = createPath(request.prefix().getFolder(), request.fileName());

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(
                PutObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMillis(expTime))
                        .putObjectRequest(r -> r.bucket(bucket).key(key))
                        .build()
        );

        return new PresignedUrlResponse(presignedRequest.url().toString(), toFileUrl(key));
    }

    /** 파일 URL 로 S3 에서 삭제. */
    public void delete(String fileUrl) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(extractKey(fileUrl))
                .build());
    }

    /** 서버에서 바이트 직접 업로드 후 최종 접근 URL(CDN) 반환. */
    public String uploadBytes(String key, byte[] data, String contentType) {
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(contentType)
                        .build(),
                RequestBody.fromBytes(data)
        );
        return toFileUrl(key);
    }

    /** key → 공개 접근 URL (CDN 기준). */
    private String toFileUrl(String key) {
        return cdnUrl + "/" + key;
    }

    /** 공개 URL → S3 object key (CDN 접두사 제거). */
    private String extractKey(String fileUrl) {
        return fileUrl.substring(cdnUrl.length() + 1);
    }

    private String createPath(String prefix, String fileName) {
        String fileId = UUID.randomUUID().toString();
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        return "%s/%s-%s-%s".formatted(prefix, timestamp, fileId, fileName);
    }
}
