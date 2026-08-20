package kr.co.dearbloom.global.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 메일 발송 설정.
 *
 * @param enabled        false 면 발송하지 않고 로그만 남긴다. 로컬·테스트에서 실제 주소로 메일이 나가는 사고를 막는다
 * @param from           발신 주소. 발송 서비스에 인증된 도메인이어야 한다
 * @param supportAddress 본문 하단에 안내할 문의처
 */
@ConfigurationProperties("app.mail")
public record MailProperties(boolean enabled, String from, String supportAddress) {
}
