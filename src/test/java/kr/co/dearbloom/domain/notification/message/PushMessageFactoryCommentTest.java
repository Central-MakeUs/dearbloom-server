package kr.co.dearbloom.domain.notification.message;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** 공동보드 댓글 알림 문구. 잠금화면에 그대로 뜨는 값이라 무엇이 담기고 무엇이 빠지는지를 고정한다. */
class PushMessageFactoryCommentTest {
    private final PushMessageFactory factory = new PushMessageFactory();

    @Test
    @DisplayName("작성자 이름과 댓글 내용이 본문에 담기고, 딥링크는 그 보드 화면을 가리킨다")
    void carriesAuthorNameAndContent() {
        PushMessage message = factory.sharedCommentCreated(7L, "김졸업", "이거 진짜 예쁘다!");

        assertThat(message.title()).isEqualTo("새 댓글이 달렸어요");
        assertThat(message.body()).isEqualTo("[김졸업] 이거 진짜 예쁘다!");
        assertThat(message.deepLink()).isEqualTo("/app/boards/7");
    }

    @Test
    @DisplayName("긴 댓글은 잘라 낸다 — 원문을 그대로 실으면 FCM 페이로드 상한에 걸릴 수 있다")
    void truncatesLongContent() {
        String longContent = "가".repeat(200);

        String body = factory.sharedCommentCreated(1L, "김졸업", longContent).body();

        assertThat(body).endsWith("…");
        assertThat(body).hasSizeLessThan(100);
    }

    @Test
    @DisplayName("줄바꿈은 한 칸 공백으로 눕힌다 — 알림은 한 줄로 보인다")
    void flattensNewlines() {
        String body = factory.sharedCommentCreated(1L, "김졸업", "첫 줄\n\n둘째 줄").body();

        assertThat(body).isEqualTo("[김졸업] 첫 줄 둘째 줄");
    }

    @Test
    @DisplayName("경계 길이(60자)는 자르지 않는다")
    void keepsContentAtBoundary() {
        String exact = "나".repeat(60);

        String body = factory.sharedCommentCreated(1L, "김졸업", exact).body();

        assertThat(body).isEqualTo("[김졸업] " + exact);
        assertThat(body).doesNotContain("…");
    }
}
