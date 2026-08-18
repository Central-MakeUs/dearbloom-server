package kr.co.dearbloom.global.auth.jwt;

import java.util.List;

/**
 * 인증 불필요 경로를 한 곳에서 관리.
 * <ul>
 *   <li>{@link #SKIP_TOKEN_PREFIXES} — 토큰 검증 자체를 스킵. 메서드 무관 permitAll</li>
 *   <li>{@link #PUBLIC_GET_PREFIXES} — <b>GET 만</b> permitAll. 같은 prefix 의 쓰기는 인증 필요</li>
 * </ul>
 * TokenAuthenticationFilter 는 startsWith 로, WebSecurityConfig 는 Ant 패턴으로 사용한다.
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
            "/dev/member/me",
            // logout 은 /api/auth/ 하위라 기본적으로 스킵 대상이지만, @AuthenticationPrincipal 로 회원을 식별해야 하므로 예외 처리
            "/api/auth/logout"
    );

    /**
     * 비로그인도 접근 가능한 <b>조회</b> 경로. 토큰이 있으면 인증을 설정하고, 없으면 비로그인으로 통과한다.
     *
     * <p>SKIP_TOKEN_PREFIXES 와 달리 토큰 검증을 <b>스킵하지 않는다</b>. 스킵하면 SecurityContext 가 비어
     * 로그인한 사용자도 게스트로 취급된다(예: 작품 목록의 isSaved 가 항상 false, 초대 화면의 alreadyJoined 도 마찬가지).
     *
     * <p><b>GET 에만 permitAll 을 건다.</b> 아래 prefix 들은 같은 경로에 쓰기 API 가 함께 있어서
     * prefix 통째로 열면 인증이 뚫린다 — {@code /api/artworks} 는 작품 등록·수정·삭제(작가 전용)를,
     * {@code /api/shared-boards/invite} 는 보드 입장(POST)을 같은 prefix 에 두고 있다.
     */
    public static final List<String> PUBLIC_GET_PREFIXES = List.of(
            "/api/artworks",
            "/api/shared-boards/invite"
    );

    /** 메서드 무관 permitAll 대상 Ant 패턴. */
    public static String[] permitAllPatterns() {
        return SKIP_TOKEN_PREFIXES.stream().map(PublicPaths::toAntPattern).toArray(String[]::new);
    }

    /** GET 만 permitAll 할 대상 Ant 패턴. */
    public static String[] publicGetPatterns() {
        return PUBLIC_GET_PREFIXES.stream().map(PublicPaths::toAntPattern).toArray(String[]::new);
    }

    // "/api/auth/" → "/api/auth/**", "/health" → "/health/**" (PathPattern 에서 "/health" 자신도 매칭된다)
    private static String toAntPattern(String prefix) {
        return prefix.endsWith("/") ? prefix + "**" : prefix + "/**";
    }
}
