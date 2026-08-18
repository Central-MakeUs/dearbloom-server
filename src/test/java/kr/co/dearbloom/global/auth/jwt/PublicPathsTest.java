package kr.co.dearbloom.global.auth.jwt;

import org.junit.jupiter.api.Test;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 공개 경로 패턴 검증.
 *
 * <p>Ant 패턴을 손으로 조립하다 보니 <b>목록 경로 자신</b>({@code /api/artworks})이 빠지거나,
 * 반대로 인증이 필요한 이웃 경로까지 열리기 쉽다. 인가를 켠 뒤에는 이런 실수가 곧바로
 * "비로그인인데 작품이 안 보인다" 또는 "인증 없이 삭제된다"로 이어지므로 테스트로 묶는다.
 */
class PublicPathsTest {
    private final PathPatternParser parser = new PathPatternParser();

    private boolean matches(String[] patterns, String path) {
        return Arrays.stream(patterns)
                .anyMatch(pattern -> parser.parse(pattern).matches(
                        org.springframework.http.server.PathContainer.parsePath(path)));
    }

    @Test
    void 공개_조회_패턴은_목록과_상세를_모두_포함한다() {
        String[] patterns = PublicPaths.publicGetPatterns();

        assertThat(matches(patterns, "/api/artworks")).isTrue();
        assertThat(matches(patterns, "/api/artworks/1")).isTrue();
        assertThat(matches(patterns, "/api/shared-boards/invite/K7QM2X")).isTrue();
    }

    @Test
    void 공개_조회_패턴이_인증_필요한_이웃_경로까지_열지_않는다() {
        String[] patterns = PublicPaths.publicGetPatterns();

        // 보드 초대코드 발급은 소유자만 볼 수 있다.
        assertThat(matches(patterns, "/api/shared-boards/1/invite-code")).isFalse();
        // 작가 본인 작품 목록은 인증이 필요하다.
        assertThat(matches(patterns, "/api/artists/me/artworks")).isFalse();
        assertThat(matches(patterns, "/api/members/me")).isFalse();
        assertThat(matches(patterns, "/api/files/presigned-url")).isFalse();
    }

    @Test
    void 토큰_검증_스킵_경로는_자신과_하위를_모두_포함한다() {
        String[] patterns = PublicPaths.permitAllPatterns();

        assertThat(matches(patterns, "/health")).isTrue();
        assertThat(matches(patterns, "/health/infra")).isTrue();
        assertThat(matches(patterns, "/api/auth/login")).isTrue();
        assertThat(matches(patterns, "/api/universities/search")).isTrue();
    }

    @Test
    void 스킵_경로가_일반_API_를_열지_않는다() {
        String[] patterns = PublicPaths.permitAllPatterns();

        assertThat(matches(patterns, "/api/artworks")).isFalse();
        assertThat(matches(patterns, "/api/files/presigned-url")).isFalse();
        assertThat(matches(patterns, "/api/members/me")).isFalse();
    }
}
