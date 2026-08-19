package kr.co.dearbloom.domain.auth.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApplePrivateRelayEmailTest {
    private static final String SUB = "001234.abcdef0123456789abcdef01234567.0000";

    @Test
    @DisplayName("지어낸 주소는 placeholder 로 판정된다 — 발송에서 걸러야 한다")
    void detectsPlaceholder() {
        String email = ApplePrivateRelayEmail.placeholderFor(SUB);

        assertThat(email).isEqualTo(SUB + "@privaterelay.appleid.com");
        assertThat(ApplePrivateRelayEmail.isPlaceholder(email, SUB)).isTrue();
    }

    @Test
    @DisplayName("Apple 이 발급한 진짜 중계 주소는 통과한다 — 같은 도메인이지만 보낼 수 있다")
    void realRelayAddressIsNotPlaceholder() {
        // "이메일 가리기" 사용자의 실제 주소. 도메인은 같지만 로컬파트가 sub 와 무관하다.
        String realRelay = "k7x9m2p4q8@privaterelay.appleid.com";

        assertThat(ApplePrivateRelayEmail.isPlaceholder(realRelay, SUB)).isFalse();
    }

    @Test
    @DisplayName("일반 주소와 null 은 placeholder 가 아니다")
    void ordinaryAddressIsNotPlaceholder() {
        assertThat(ApplePrivateRelayEmail.isPlaceholder("user@gmail.com", SUB)).isFalse();
        assertThat(ApplePrivateRelayEmail.isPlaceholder(null, SUB)).isFalse();
        assertThat(ApplePrivateRelayEmail.isPlaceholder("user@gmail.com", null)).isFalse();
    }
}
