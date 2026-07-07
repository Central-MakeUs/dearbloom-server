package kr.co.dearbloom.global.file;

import kr.co.dearbloom.global.dto.response.exception.CustomException;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 파일 URL 이 dearbloom S3 경로로 시작하는지 검증한다.
 * 위반 시 400 Bad Request (INVALID_IMAGE_URL) 로 응답되도록 CustomException 을 던진다.
 */
@Component
public class FileUrlValidator {

    private final String requiredPrefix;

    public FileUrlValidator(@Value("${file.cdn-url}") String cdnUrl) {
        this.requiredPrefix = cdnUrl + "/";
    }

    public void validate(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank() || !fileUrl.startsWith(requiredPrefix)) {
            throw new CustomException(ErrorCode.INVALID_IMAGE_URL);
        }
    }

    public void validateAll(List<String> fileUrls) {
        if (fileUrls == null || fileUrls.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_IMAGE_URL);
        }
        for (String url : fileUrls) {
            validate(url);
        }
    }
}
