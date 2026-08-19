package kr.co.dearbloom.domain.notification.message;

import kr.co.dearbloom.domain.auth.entity.OAuthProvider;
import kr.co.dearbloom.domain.member.entity.MemberRole;
import kr.co.dearbloom.global.properties.MailProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SignUpMailFactoryTest {
    private final SignUpMailFactory factory = new SignUpMailFactory(
            new MailProperties(true, "no-reply@dearbloom.co.kr", "dearbloom.dev@gmail.com"),
            "https://dearbloom.co.kr");

    @Test
    @DisplayName("고객은 화면 표기대로 '모델' 로 안내한다 — 내부 role 이름을 노출하지 않는다")
    void customerIsLabeledAsModel() {
        MailMessage mail = factory.signUp("김졸업", MemberRole.CUSTOMER, OAuthProvider.GOOGLE);

        assertThat(mail.subject()).isEqualTo("김졸업님, 디어블룸 가입을 환영합니다.");
        assertThat(mail.text())
                .contains("김졸업")
                .contains("모델")
                .contains("Google 계정")
                .doesNotContain("CUSTOMER");
    }

    @Test
    @DisplayName("작가는 '작가', Apple 로그인은 'Apple 계정' 으로 안내한다")
    void artistWithApple() {
        MailMessage mail = factory.signUp("블룸작가", MemberRole.ARTIST, OAuthProvider.APPLE);

        assertThat(mail.text())
                .contains("블룸작가")
                .contains("작가")
                .contains("Apple 계정")
                .doesNotContain("ARTIST");
    }

    @Test
    @DisplayName("서비스 링크와 문의처가 본문에 들어간다")
    void includesServiceUrlAndSupportAddress() {
        MailMessage mail = factory.signUp("김졸업", MemberRole.CUSTOMER, OAuthProvider.GOOGLE);

        assertThat(mail.text())
                .contains("https://dearbloom.co.kr")
                .contains("dearbloom.dev@gmail.com");
    }

    @Test
    @DisplayName("디자인 적용 전이라 HTML 은 없다 — 텍스트로만 나간다")
    void textOnlyForNow() {
        assertThat(factory.signUp("김졸업", MemberRole.CUSTOMER, OAuthProvider.GOOGLE).hasHtml()).isFalse();
    }
}
