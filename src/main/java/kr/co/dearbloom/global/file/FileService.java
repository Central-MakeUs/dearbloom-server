package kr.co.dearbloom.global.file;

import kr.co.dearbloom.global.file.dto.PresignedUrlResponse;

/**
 * 파일(이미지/영상 등) 업로드·삭제를 위한 스토리지 추상화.
 * <p>
 * 컨트롤러·도메인은 이 인터페이스에만 의존하고, 실제 인프라(S3 등)는 구현체로 격리한다.
 * 저장소가 CloudFront/GCS 등으로 바뀌어도 API 는 흔들리지 않는다.
 */
public interface FileService {

    /**
     * 클라이언트가 파일을 직접 업로드할 수 있는 Presigned URL 과 최종 접근 URL 을 발급한다.
     */
    PresignedUrlResponse getUploadPresignedUrl(String prefix, String fileName);

    /**
     * 서버에서 바이트 데이터를 직접 업로드하고 최종 접근 URL 을 반환한다.
     */
    String uploadBytes(String key, byte[] data, String contentType);

    /**
     * 파일 URL 로 저장소에서 해당 파일을 삭제한다.
     */
    void delete(String fileUrl);
}
