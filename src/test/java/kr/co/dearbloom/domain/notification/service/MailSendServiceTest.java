package kr.co.dearbloom.domain.notification.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MailSendServiceTest {
    @Test
    @DisplayName("개발 서버 메일은 제목 앞에 환경이 표시된다")
    void prefixesOnDevelopmentServer() {
        assertThat(MailSendService.subjectPrefixFor("개발")).isEqualTo("(개발 환경) ");
    }

    @Test
    @DisplayName("운영 서버 메일은 제목이 그대로 나간다")
    void noPrefixOnProduction() {
        assertThat(MailSendService.subjectPrefixFor("운영")).isEmpty();
    }

    @Test
    @DisplayName("모르는 환경 이름은 표시하는 쪽으로 둔다 — 운영으로 오인되면 안 된다")
    void prefixesUnknownEnvironment() {
        assertThat(MailSendService.subjectPrefixFor("스테이징")).isEqualTo("(스테이징 환경) ");
    }

    @Test
    @DisplayName("환경 이름이 비어 있으면 붙이지 않는다 — 제목이 '( 환경) ' 으로 시작하지 않게 한다")
    void ignoresBlankEnvironmentName() {
        assertThat(MailSendService.subjectPrefixFor(null)).isEmpty();
        assertThat(MailSendService.subjectPrefixFor("  ")).isEmpty();
    }
}
