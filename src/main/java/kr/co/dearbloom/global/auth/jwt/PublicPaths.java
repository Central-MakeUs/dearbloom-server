package kr.co.dearbloom.global.auth.jwt;

import java.util.List;

/**
 * 인증 불필요 경로를 한 곳에서 관리.
 * - TokenAuthenticationFilter: SKIP_TOKEN_PREFIXES 로 startsWith 매칭
 * - WebSecurityConfig: permitAllPatterns() / optionalAuthGetPatterns() 로 requestMatchers 에 사용
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

    /**
     * 비로그인도 조회(GET) 가능 — 토큰 있으면 인증 설정, 없으면 비로그인으로 통과.
     * - 토큰 검증을 스킵하지 않는다: isSaved 같은 뷰어별 값을 채우려면 인증 설정이 필요하다.
     * - GET 에만 permitAll: 같은 prefix 에 작가 전용 쓰기 API 가 공존한다(POST /api/artworks 등).
     */
    public static final List<String> OPTIONAL_AUTH_GET_PREFIXES = List.of(
            "/api/artworks"   // 작품 리스트·상세 (비로그인 열람 허용, 고객이면 isSaved 채움)
    );

    /** 메서드 무관 permitAll 대상 (인증 자체가 불필요한 경로) */
    public static String[] permitAllPatterns() {
        return toAntPatterns(SKIP_TOKEN_PREFIXES);
    }

    /** GET 에 한해 permitAll 하는 대상 (인증이 불필요한 게 아니라 선택적인 경로) */
    public static String[] optionalAuthGetPatterns() {
        return toAntPatterns(OPTIONAL_AUTH_GET_PREFIXES);
    }

    private static String[] toAntPatterns(List<String> prefixes) {
        return prefixes.stream()
                .map(p -> p.endsWith("/") ? p + "**" : p + "/**")
                .toArray(String[]::new);
    }
}
