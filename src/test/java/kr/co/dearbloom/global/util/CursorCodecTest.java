package kr.co.dearbloom.global.util;

import kr.co.dearbloom.domain.artwork.dto.ArtworkCursor;
import kr.co.dearbloom.global.dto.response.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 커서가 왕복하지 않으면 모든 목록의 2페이지가 깨지므로 인코딩/디코딩만 따로 검증한다.
 * Jackson 3 부터는 java.time 지원이 기본이라 별도 모듈 등록 없이 Boot 가 주는 매퍼와 같은 동작을 한다.
 */
class CursorCodecTest {
    private final CursorCodec cursorCodec = new CursorCodec(new ObjectMapper());

    @Test
    @DisplayName("커서를 인코딩했다가 디코딩하면 원래 값이 그대로 나온다")
    void 커서가_왕복한다() {
        ArtworkCursor cursor = new ArtworkCursor(
                LocalDateTime.of(2026, 6, 11, 9, 41, 0), 200000, 31L);

        ArtworkCursor decoded = cursorCodec.decode(cursorCodec.encode(cursor), ArtworkCursor.class);

        assertThat(decoded).isEqualTo(cursor);
    }

    @Test
    @DisplayName("URL 에 그대로 실을 수 있게 인코딩된다(패딩·특수문자 없음)")
    void 인코딩_결과는_URL_안전하다() {
        String encoded = cursorCodec.encode(
                new ArtworkCursor(LocalDateTime.of(2026, 6, 11, 9, 41, 0), 200000, 31L));

        assertThat(encoded).matches("[A-Za-z0-9_-]+");
    }

    @Test
    @DisplayName("커서가 비어 있으면(첫 페이지) null 을 돌려준다")
    void 빈_커서는_null() {
        assertThat(cursorCodec.decode(null, ArtworkCursor.class)).isNull();
        assertThat(cursorCodec.decode("", ArtworkCursor.class)).isNull();
        assertThat(cursorCodec.decode("   ", ArtworkCursor.class)).isNull();
    }

    @Test
    @DisplayName("깨진 커서는 400 으로 떨어진다")
    void 깨진_커서는_예외() {
        assertThatThrownBy(() -> cursorCodec.decode("not-a-cursor!!", ArtworkCursor.class))
                .isInstanceOf(CustomException.class);
    }
}
