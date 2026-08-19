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
    @DisplayName("HTML 과 텍스트 대체본이 함께 만들어진다 — HTML 만 보내면 못 읽는 클라이언트가 빈 화면이 된다")
    void buildsBothHtmlAndText() {
        MailMessage mail = factory.signUp("김졸업", MemberRole.CUSTOMER, OAuthProvider.GOOGLE);

        assertThat(mail.hasHtml()).isTrue();
        assertThat(mail.text()).isNotBlank();
    }

    @Test
    @DisplayName("HTML 에 치환되지 않은 자리표시자가 남지 않는다")
    void leavesNoUnreplacedPlaceholder() {
        String html = factory.signUp("김졸업", MemberRole.ARTIST, OAuthProvider.APPLE).html();

        assertThat(html).doesNotContain("{{");
    }

    @Test
    @DisplayName("HTML 에도 프로필명·가입 유형·로그인 방식이 들어간다")
    void htmlCarriesTheThreeFields() {
        String html = factory.signUp("블룸작가", MemberRole.ARTIST, OAuthProvider.APPLE).html();

        assertThat(html)
                .contains("블룸작가")
                .contains("작가")
                .contains("Apple 계정")
                .contains("https://dearbloom.co.kr")
                .contains("dearbloom.dev@gmail.com");
    }

    @Test
    @DisplayName("이름의 특수문자는 이스케이프된다 — 마크업이 깨지거나 태그가 주입되면 안 된다")
    void escapesProfileName() {
        String html = factory.signUp("<b>김</b>", MemberRole.CUSTOMER, OAuthProvider.GOOGLE).html();

        assertThat(html).contains("&lt;b&gt;김&lt;/b&gt;");
        assertThat(html).doesNotContain("<b>김</b>");
    }

    @Test
    @DisplayName("표 레이아웃의 width=\"100%\" 가 그대로 남는다 — 포맷 문자열로 다루다 깨지기 쉬운 지점")
    void keepsPercentWidthIntact() {
        String html = factory.signUp("김졸업", MemberRole.CUSTOMER, OAuthProvider.GOOGLE).html();

        assertThat(html).contains("width=\"100%\"");
    }
}
