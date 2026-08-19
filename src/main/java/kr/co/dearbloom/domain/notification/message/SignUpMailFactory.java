package kr.co.dearbloom.domain.notification.message;

import kr.co.dearbloom.domain.auth.entity.OAuthProvider;
import kr.co.dearbloom.domain.member.entity.MemberRole;
import kr.co.dearbloom.global.properties.MailProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 가입 안내 메일의 제목·본문 생성.
 *
 * <p>지금은 텍스트만 만든다. HTML 디자인이 나오면 {@code html} 을 채워 {@link MailMessage} 로 함께 넘기면 되고,
 * 발송 경로는 그대로 둬도 된다.
 */
@Component
public class SignUpMailFactory {
    private final MailProperties properties;
    private final String serviceUrl;

    // 필드 주입 대신 생성자로 받는다 — 본문 문구는 테스트로 고정할 값이라 컨텍스트 없이 만들 수 있어야 한다.
    public SignUpMailFactory(MailProperties properties, @Value("${app.service-url}") String serviceUrl) {
        this.properties = properties;
        this.serviceUrl = serviceUrl;
    }

    public MailMessage signUp(String profileName, MemberRole role, OAuthProvider provider) {
        String subject = "%s님, 디어블룸 가입을 환영합니다.".formatted(profileName);
        String text = """
                %s님, 디어블룸 가입을 환영합니다.

                디어블룸 가입이 완료되었습니다.
                아래 정보로 계정이 생성되었어요.

                  프로필명    %s
                  가입 유형    %s
                  로그인 방식  %s

                디어블룸 바로가기: %s

                ---
                본 메일은 DearBloom 회원가입 안내 메일로, 발신 전용입니다.
                문의사항은 %s으로 연락해 주세요.
                """.formatted(
                profileName, profileName, roleLabel(role), providerLabel(provider),
                serviceUrl, properties.supportAddress());

        return MailMessage.textOnly(subject, text);
    }

    /** 화면에서 고객은 "모델" 로 부른다 — 내부 role 이름을 그대로 노출하지 않는다. */
    private String roleLabel(MemberRole role) {
        return switch (role) {
            case CUSTOMER -> "모델";
            case ARTIST -> "작가";
        };
    }

    private String providerLabel(OAuthProvider provider) {
        return switch (provider) {
            case GOOGLE -> "Google 계정";
            case APPLE -> "Apple 계정";
        };
    }
}
