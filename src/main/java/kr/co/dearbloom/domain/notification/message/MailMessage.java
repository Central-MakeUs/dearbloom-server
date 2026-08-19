package kr.co.dearbloom.domain.notification.message;

/**
 * 보낼 메일 한 통. 제목과 본문만 담는다 — 받는 사람은 발송 시점에 정해진다.
 *
 * <p>HTML 과 텍스트를 함께 담는다. HTML 만 보내면 텍스트만 지원하는 클라이언트에서 빈 화면이 되고
 * 스팸 점수도 올라간다. 아직 디자인 전이라 지금은 {@code html} 이 비어 있을 수 있다(그러면 텍스트로만 나간다).
 */
public record MailMessage(String subject, String text, String html) {
    public static MailMessage textOnly(String subject, String text) {
        return new MailMessage(subject, text, null);
    }

    public boolean hasHtml() {
        return html != null && !html.isBlank();
    }
}
