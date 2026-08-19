package kr.co.dearbloom.domain.notification.message;

import kr.co.dearbloom.domain.auth.entity.OAuthProvider;
import kr.co.dearbloom.domain.member.entity.MemberRole;
import kr.co.dearbloom.global.properties.MailProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 가입 안내 메일의 제목·본문 생성. HTML 과 텍스트 대체본을 함께 만든다.
 *
 * <p>HTML 이 메일 치고 낡아 보이는 이유가 있다 — 메일 클라이언트는 브라우저가 아니다.
 * Outlook 데스크톱은 Word 렌더링 엔진이라 flex·grid 를 모르고, Gmail 은 {@code <style>} 태그와
 * {@code <svg>} 를 지운다. 그래서 <b>표 레이아웃 + 인라인 스타일 + 이미지</b> 로만 짠다.
 * 폰트도 마찬가지라 Pretendard 는 로드되지 않는 곳이 많고, 폴백 스택이 실제로 쓰인다.
 *
 * <p>버튼과 로고는 CDN 이미지다. 메일 클라이언트는 이미지를 기본 차단하는 경우가 많아
 * <b>{@code alt} 가 실제로 화면에 보인다</b> — 버튼은 {@code <a>} 로 감싸 이미지가 막혀도 링크가 살아 있게 했다.
 */
@Component
public class SignUpMailFactory {
    /** 메일용 브랜드 이미지. 환경과 무관한 고정 자산이라 운영 CDN 을 그대로 쓴다. */
    private static final String LOGO_URL = "https://cdn.dearbloom.co.kr/mail/dearbloom_email_logo.png";
    private static final String BUTTON_URL = "https://cdn.dearbloom.co.kr/mail/dearbloom_email_connect.png";

    private final MailProperties properties;
    private final String serviceUrl;

    // 필드 주입 대신 생성자로 받는다 — 본문 문구는 테스트로 고정할 값이라 컨텍스트 없이 만들 수 있어야 한다.
    public SignUpMailFactory(MailProperties properties, @Value("${app.service-url}") String serviceUrl) {
        this.properties = properties;
        this.serviceUrl = serviceUrl;
    }

    public MailMessage signUp(String profileName, MemberRole role, OAuthProvider provider) {
        String subject = "%s님, 디어블룸 가입을 환영합니다.".formatted(profileName);
        return new MailMessage(
                subject,
                text(profileName, role, provider),
                html(profileName, role, provider));
    }

    /**
     * HTML 을 못 읽는 클라이언트용 대체본. 없으면 그런 클라이언트에서 빈 화면이 되고 스팸 점수도 올라간다.
     */
    private String text(String profileName, MemberRole role, OAuthProvider provider) {
        return """
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
    }

    /**
     * 치환은 {@code String.formatted} 가 아니라 이름표({@code {{...}}})로 한다 —
     * 템플릿에 {@code width="100%"} 처럼 {@code %} 가 들어 있어 포맷 문자열로 다루면 escape 를 놓치기 쉽다.
     */
    private String html(String profileName, MemberRole role, OAuthProvider provider) {
        return SIGN_UP_HTML
                .replace("{{logoUrl}}", LOGO_URL)
                .replace("{{buttonUrl}}", BUTTON_URL)
                .replace("{{serviceUrl}}", serviceUrl)
                .replace("{{supportAddress}}", properties.supportAddress())
                .replace("{{profileName}}", escapeHtml(profileName))
                .replace("{{roleLabel}}", roleLabel(role))
                .replace("{{providerLabel}}", providerLabel(provider));
    }

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

    /**
     * 이름은 사용자가 정한 값이라 그대로 넣으면 마크업이 깨질 수 있다.
     * 지금은 입력 검증이 한글·영문·숫자만 허용해 이런 문자가 들어올 수 없지만,
     * 검증이 느슨해지는 순간 메일이 조용히 망가지므로 여기서 한 번 더 막는다.
     */
    private String escapeHtml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static final String SIGN_UP_HTML = """
            <!DOCTYPE html>
            <html lang="ko">
            <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <meta name="color-scheme" content="light">
            <meta name="supported-color-schemes" content="light">
            <title>DearBloom 가입 안내</title>
            <style>
              @font-face {
                font-family: 'Pretendard';
                font-weight: 500;
                font-style: normal;
                src: url('https://cdn.jsdelivr.net/gh/orioncactus/pretendard@v1.3.9/dist/web/static/woff2/Pretendard-Medium.woff2') format('woff2');
              }
              @font-face {
                font-family: 'Pretendard';
                font-weight: 700;
                font-style: normal;
                src: url('https://cdn.jsdelivr.net/gh/orioncactus/pretendard@v1.3.9/dist/web/static/woff2/Pretendard-Bold.woff2') format('woff2');
              }
              body { margin: 0; padding: 0; background-color: #FFFFFF; -webkit-text-size-adjust: 100%; -ms-text-size-adjust: 100%; }
              table { border-collapse: collapse; }  /* 단, 모서리를 둥글릴 표는 인라인으로 separate 를 준다 — collapse 상태에선 border-radius 가 무시된다 */
              img { -ms-interpolation-mode: bicubic; }
            </style>
            </head>
            <body style="margin:0; padding:0; background-color:#FFFFFF;">

            <!-- 바깥 여백. 배경은 흰색이고, 카드 구분은 카드 자체의 테두리가 맡는다.
                 배경색 대신 테두리를 쓰는 이유 — Gmail 은 <body> 를 걷어내고 다크 모드에서 배경을 뒤집기도 해서
                 배경색은 환경에 따라 사라지지만, 테두리는 거의 모든 클라이언트에서 그대로 그려진다. -->
            <table role="presentation" width="100%" cellpadding="0" cellspacing="0" border="0" bgcolor="#FFFFFF"
                   style="width:100%; background-color:#FFFFFF; margin:0; padding:0;">
              <tr>
                <!-- 카드가 수신함 가장자리에 붙지 않도록 띄운다. 좌우 16px 은 모바일에서 테두리가 잘리지 않게 하는 여백이다. -->
                <td align="center" style="padding:32px 16px;">

            <table role="presentation" width="498" cellpadding="0" cellspacing="0" border="0" align="center"
                   style="width:100%; max-width:498px; -webkit-text-size-adjust:100%; -ms-text-size-adjust:100%; background-color:#FFFFFF; margin:0 auto;
                          border:1px solid #EAEAEA; border-radius:16px;
                          border-collapse:separate; border-spacing:0;
                          font-family:'Pretendard',-apple-system,BlinkMacSystemFont,'Apple SD Gothic Neo','Malgun Gothic','맑은 고딕','Noto Sans KR',sans-serif;">
              <tr>
                <td style="padding:40px 28px 0 28px;">
                  <img src="{{logoUrl}}" width="116" alt="DearBloom"
                       style="display:block; width:116px; height:auto; border:0; outline:none; text-decoration:none;">
                </td>
              </tr>

              <tr>
                <td style="padding:24px 28px 0 28px;">
                  <div style="font-weight:700; font-size:20px; line-height:28px; letter-spacing:-0.005em;">
                    <span style="color:#296A48;">{{profileName}}</span><span style="color:#2A2A2A;">님, 디어블룸 가입을</span><br>
                    <span style="color:#2A2A2A;">환영합니다.</span>
                  </div>
                </td>
              </tr>

              <tr>
                <td style="padding:12px 28px 0 28px;">
                  <div style="font-weight:500; font-size:14px; line-height:21px; letter-spacing:0; color:#5C5C5C;">
                    디어블룸 가입이 완료되었습니다.<br>
                    아래 정보로 계정이 생성되었어요.
                  </div>
                </td>
              </tr>

              <tr>
                <td style="padding:24px 28px 0 28px;">
                  <table role="presentation" width="100%" cellpadding="0" cellspacing="0" border="0"
                         style="width:100%; background-color:#EEF3F0; border-radius:8px; border-collapse:separate; border-spacing:0;">
                    <tr>
                      <td style="padding:20px 24px;">
                        <table role="presentation" width="100%" cellpadding="0" cellspacing="0" border="0">
                          <tr>
                            <td width="80" valign="top" style="padding-bottom:16px; font-weight:500; font-size:12px; line-height:12.6px; letter-spacing:0; color:#4F7F63;">프로필명</td>
                            <td valign="top" style="padding-bottom:16px; font-weight:500; font-size:12px; line-height:12.6px; letter-spacing:0; color:#1F1F1F;">{{profileName}}</td>
                          </tr>
                          <tr>
                            <td width="80" valign="top" style="padding-bottom:16px; font-weight:500; font-size:12px; line-height:12.6px; letter-spacing:0; color:#4F7F63;">가입 유형</td>
                            <td valign="top" style="padding-bottom:16px; font-weight:500; font-size:12px; line-height:12.6px; letter-spacing:0; color:#1F1F1F;">{{roleLabel}}</td>
                          </tr>
                          <tr>
                            <td width="80" valign="top" style="font-weight:500; font-size:12px; line-height:12.6px; letter-spacing:0; color:#4F7F63;">로그인 방식</td>
                            <td valign="top" style="font-weight:500; font-size:12px; line-height:12.6px; letter-spacing:0; color:#1F1F1F;">{{providerLabel}}</td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </td>
              </tr>

              <tr>
                <td align="center" style="padding:40px 28px 0 28px;">
                  <a href="{{serviceUrl}}" target="_blank" style="display:inline-block; text-decoration:none;">
                    <img src="{{buttonUrl}}" width="118" alt="디어블룸 바로가기"
                         style="display:block; width:118px; height:auto; border:0; outline:none; text-decoration:none; color:#296A48; font-size:14px; font-weight:700;">
                  </a>
                </td>
              </tr>

              <tr>
                <td style="padding:48px 28px 0 28px;">
                  <table role="presentation" width="100%" cellpadding="0" cellspacing="0" border="0" style="width:100%;">
                    <tr><td style="border-top:1px solid #EAEAEA; font-size:0; line-height:0;">&nbsp;</td></tr>
                  </table>
                </td>
              </tr>

              <tr>
                <td style="padding:24px 28px 40px 28px;">
                  <div style="font-weight:500; font-size:12px; line-height:18px; letter-spacing:0; color:#767676;">
                    본 메일은 DearBloom 회원가입 안내 메일로, 발신 전용입니다.<br>
                    문의사항은 {{supportAddress}}으로 연락해 주세요.
                  </div>
                </td>
              </tr>
            </table>

                </td>
              </tr>
            </table>

            </body>
            </html>
            """;
}
