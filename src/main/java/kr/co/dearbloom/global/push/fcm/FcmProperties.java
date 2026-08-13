package kr.co.dearbloom.global.push.fcm;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * FCM HTTP v1 설정. 서비스 계정 JSON 에서 필요한 세 값만 뽑아 환경변수로 받는다
 * (JSON 파일을 통째로 배포에 얹지 않기 위해서다).
 *
 * @param enabled     false 면 실제 발송 없이 로그만 남긴다. 로컬·테스트 기본값
 * @param projectId   Firebase 프로젝트 ID. 엔드포인트 경로에 들어간다
 * @param clientEmail 서비스 계정 이메일. access token 을 받을 JWT 의 iss/sub
 * @param privateKey  서비스 계정 개인키 PEM. RS256 서명용. 개행이 {@code \n} 으로 escape 돼 있어도 된다
 */
@ConfigurationProperties(prefix = "push.fcm")
public record FcmProperties(boolean enabled, String projectId, String clientEmail, String privateKey) {
    public boolean isConfigured() {
        return notBlank(projectId) && notBlank(clientEmail) && notBlank(privateKey);
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }
}
