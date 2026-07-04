package kr.co.dearbloom.global.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 토큰 쿠키 속성. 환경별로 도메인/Secure를 분기한다.
 * - 로컬: domain 미설정(host-only localhost, 포트 무관 공유), secure=false
 * - 개발·운영: domain=.dearbloom.co.kr (FE·BE가 apex만 공유하므로 apex로 지정), secure=true
 */
@Setter
@Getter
@Component
@ConfigurationProperties("cookie")
public class CookieProperties {
    private String domain;            // 비어있으면 Domain 속성 생략(host-only)
    private boolean secure;
    private String sameSite = "Lax";
}
