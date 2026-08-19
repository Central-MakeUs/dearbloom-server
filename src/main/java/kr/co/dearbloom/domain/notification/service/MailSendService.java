package kr.co.dearbloom.domain.notification.service;

import jakarta.mail.internet.MimeMessage;
import kr.co.dearbloom.domain.notification.message.MailMessage;
import kr.co.dearbloom.global.properties.MailProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * SMTP 발송. 발송처(SES/Gmail 등)는 {@code spring.mail.*} 설정으로만 갈리고 이 코드는 바뀌지 않는다.
 *
 * <p>예외를 밖으로 내보내지 않는다. 이 서비스를 부르는 쪽은 이미 커밋된 작업(가입 등)의 후처리라,
 * 메일 실패로 그 작업을 되돌릴 수도 없고 되돌려서도 안 된다.
 */
@Slf4j
@Service
public class MailSendService {
    /** 수신함에 표시될 발신자 이름. 서비스명이라 환경별로 갈리지 않는다. */
    private static final String FROM_NAME = "DearBloom";
    /** {@code app.server-env-name} 이 이 값이면 운영으로 보고 제목에 환경을 표시하지 않는다. */
    private static final String PRODUCTION_ENV_NAME = "운영";

    private final JavaMailSender mailSender;
    private final MailProperties properties;
    private final String subjectPrefix;

    public MailSendService(JavaMailSender mailSender, MailProperties properties,
                           @Value("${app.server-env-name}") String serverEnvName) {
        this.mailSender = mailSender;
        this.properties = properties;
        this.subjectPrefix = subjectPrefixFor(serverEnvName);
    }

    /**
     * 운영이 아니면 제목 앞에 환경을 표시한다 — 수신함에서 어느 서버가 보낸 메일인지 바로 갈린다.
     * 모르는 환경 이름은 표시하는 쪽으로 둔다. 접두가 잘못 붙는 것보다 운영인 줄 알고 넘어가는 쪽이 나쁘다.
     */
    static String subjectPrefixFor(String serverEnvName) {
        if (serverEnvName == null || serverEnvName.isBlank() || PRODUCTION_ENV_NAME.equals(serverEnvName)) {
            return "";
        }
        return "(" + serverEnvName + " 환경) ";
    }

    /**
     * 한 통 발송. 실패하면 로그만 남긴다.
     *
     * @param to 받는 주소. <b>호출 전에 발송 가능한 주소인지 확인해야 한다</b>
     *           (배달 불가 주소로 보내면 반송률이 올라 발송 자격을 잃는다)
     */
    public void send(String to, MailMessage message) {
        if (!properties.enabled()) {
            log.info("[Mail] 발송 비활성 상태 — 보내지 않음: to={}, subject={}",
                    to, subjectPrefix + message.subject());
            return;
        }
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(mimeMessage, message.hasHtml(), StandardCharsets.UTF_8.name());
            helper.setFrom(properties.from(), FROM_NAME);
            helper.setTo(to);
            helper.setSubject(subjectPrefix + message.subject());
            if (message.hasHtml()) {
                // 텍스트를 함께 실어야 HTML 을 못 읽는 클라이언트에서 빈 화면이 되지 않는다.
                helper.setText(message.text(), message.html());
            } else {
                helper.setText(message.text(), false);
            }
            mailSender.send(mimeMessage);
            log.info("[Mail] 발송 완료 — subject={}", message.subject());
        } catch (UnsupportedEncodingException | jakarta.mail.MessagingException | RuntimeException e) {
            log.warn("[Mail] 발송 실패 — subject={}, {}", message.subject(), e.getMessage());
        }
    }
}
