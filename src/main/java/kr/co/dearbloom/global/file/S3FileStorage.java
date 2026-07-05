package kr.co.dearbloom.global.file;

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
 * {@link FileService} 의 S3 구현체. AWS S3 의존을 이 클래스 한 곳에 격리한다.
 */
@Service
@RequiredArgsConstructor
public class S3FileStorage implements FileService {

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${spring.cloud.aws.s3.exp-time}")
    private Long expTime;

    /** 파일 접근 base URL (URL 조립의 단일 출처). */
    @Value("${file.base-url}")
    private String baseUrl;

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Override
    public PresignedUrlResponse getUploadPresignedUrl(String prefix, String fileName) {
        String key = createPath(prefix, fileName);

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(
                PutObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMillis(expTime))
                        .putObjectRequest(r -> r.bucket(bucket).key(key))
                        .build()
        );

        String presignedUrl = presignedRequest.url().toString();
        String fileUrl = toFileUrl(key);
        return new PresignedUrlResponse(presignedUrl, fileUrl);
    }

    @Override
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

    @Override
    public void delete(String fileUrl) {
        String key = extractKey(fileUrl);
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
    }

    /** key → 공개 접근 URL (base-url 기준). */
    private String toFileUrl(String key) {
        return baseUrl + "/" + key;
    }

    /** 공개 URL → S3 object key (base-url 접두사 제거). */
    private String extractKey(String fileUrl) {
        return fileUrl.substring(baseUrl.length() + 1);
    }

    private String createPath(String prefix, String fileName) {
        String fileId = UUID.randomUUID().toString();
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        return "%s/%s-%s-%s".formatted(prefix, timestamp, fileId, fileName);
    }
}
