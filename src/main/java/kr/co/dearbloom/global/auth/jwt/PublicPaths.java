package kr.co.dearbloom.global.auth.jwt;

import java.util.List;
import java.util.stream.Stream;

/**
 * 인증 불필요 경로를 한 곳에서 관리.
 * - TokenAuthenticationFilter: PREFIXES로 startsWith 매칭
 * - WebSecurityConfig: antPatterns()로 requestMatchers에 사용
 */
public final class PublicPaths {

    private PublicPaths() {}

    /** 인증 불필요 — 토큰 검증 자체를 스킵 (TokenAuthenticationFilter) */
    public static final List<String> SKIP_TOKEN_PREFIXES = List.of(
            "/health",
            "/dev/",
            "/api/auth/",
            "/api/universities/",
            "/swagger-ui/",
            "/v3/api-docs/"
    );

    /** SKIP_TOKEN_PREFIXES 에 매칭되더라도 예외적으로 토큰 검증을 수행하는 경로 (예: dev 인증 확인용) */
    public static final List<String> SKIP_TOKEN_EXCEPTIONS = List.of(
            "/dev/member/me"
    );

    /** 비로그인도 접근 가능 — 토큰 있으면 인증 설정, 없으면 비로그인으로 통과 */
    public static final List<String> OPTIONAL_AUTH_PREFIXES = List.of(
            // tmp
            "/api/discover/",
            "/api/contents/"
    );

    /** Spring Security requestMatchers 용 Ant 패턴 (permitAll 대상 전체) */
    public static String[] antPatterns() {
        return Stream.concat(
                SKIP_TOKEN_PREFIXES.stream(),
                OPTIONAL_AUTH_PREFIXES.stream()
        ).map(p -> p.endsWith("/") ? p + "**" : p + "/**").toArray(String[]::new);
    }
}
