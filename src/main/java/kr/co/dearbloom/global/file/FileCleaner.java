package kr.co.dearbloom.global.file;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * S3 객체를 <b>실패해도 흐름을 막지 않고</b> 지운다. 회원 탈퇴처럼 되돌릴 수 없는 작업에서 쓴다.
 * <p>
 * S3 는 외부 호출이라 일시 장애로 실패할 수 있는데, 그것 때문에 탈퇴 트랜잭션을 롤백하면
 * 사용자가 탈퇴를 못 하게 된다. 남은 객체는 로그로 추적해 수동 정리하는 편이 낫다
 * (Apple 토큰 revoke 를 실패해도 탈퇴를 진행하는 것과 같은 판단).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileCleaner {
    private final FileService fileService;

    public void deleteQuietly(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }
        try {
            fileService.delete(fileUrl);
        } catch (Exception e) {
            // 지우지 못한 객체는 이 로그가 유일한 단서다 — URL 을 반드시 남긴다.
            log.warn("[FileCleaner] S3 객체 삭제 실패(무시하고 진행) — url={}, {}", fileUrl, e.getMessage());
        }
    }

    public void deleteAllQuietly(Collection<String> fileUrls) {
        if (fileUrls == null) {
            return;
        }
        fileUrls.forEach(this::deleteQuietly);
    }
}
