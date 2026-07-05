package kr.co.dearbloom.domain.auth.dto;

import kr.co.dearbloom.domain.auth.entity.OAuthProvider;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * 경로변수 등의 String 을 {@link OAuthProvider} 로 변환 (대소문자 무시).
 * 클라이언트가 "google" 처럼 소문자로 보내도 GOOGLE 로 매핑된다.
 * 미지원 값이면 IllegalArgumentException → 400 (REQUEST-400).
 * (Spring Boot 가 Converter 빈을 MVC ConversionService 에 자동 등록)
 */
@Component
public class StringToOAuthProviderConverter implements Converter<String, OAuthProvider> {

    @Override
    public OAuthProvider convert(String source) {
        return OAuthProvider.valueOf(source.trim().toUpperCase());
    }
}
