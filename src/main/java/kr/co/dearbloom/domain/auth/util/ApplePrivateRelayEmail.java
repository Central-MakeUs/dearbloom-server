package kr.co.dearbloom.domain.auth.util;

/**
 * Apple 로그인에서 이메일을 받지 못했을 때 채워 넣는 대체 주소와, 그 주소를 되짚어 내는 판정.
 *
 * <p>Apple 은 사용자가 이메일 공유에 동의했을 때만 {@code email} 클레임을 준다.
 * "이메일 가리기" 를 골라도 <b>중계 주소는 준다</b> — 그건 실제로 배달되는 주소이므로 그대로 쓰면 된다.
 * 클레임 자체가 없을 때만 여기서 만든 주소가 들어가고, <b>그 주소는 실재하지 않는다.</b>
 *
 * <p>{@code OAuthAccount.email} 이 NOT NULL 이라 값을 비워 둘 수 없어 이런 형태가 됐다.
 * 프로필 조회에는 그대로 노출하되, <b>메일 발송 전에는 반드시 걸러야 한다</b> — 보내면 전량 반송되고
 * 반송률이 오르면 발송 서비스가 계정을 정지시킨다.
 *
 * <p><b>도메인만으로 거르면 안 된다.</b> 진짜 중계 주소도 같은 도메인을 쓰기 때문에,
 * 도메인으로 거르면 정작 보낼 수 있는 "이메일 가리기" 사용자들이 통째로 막힌다.
 * 대신 로컬파트가 {@code sub}(= {@code OAuthAccount.oauthId}) 와 같은지로 가른다 —
 * Apple 이 발급하는 중계 주소의 로컬파트는 sub 와 무관한 난수라 절대 일치하지 않는다.
 *
 * <p>만드는 쪽과 거르는 쪽이 떨어져 있으면 한쪽 형식만 바뀌었을 때 조용히 전부 발송되므로 한 곳에 둔다.
 */
public final class ApplePrivateRelayEmail {
    private static final String RELAY_DOMAIN = "@privaterelay.appleid.com";

    private ApplePrivateRelayEmail() {}

    /** Apple 이 email 클레임을 주지 않았을 때 대신 채워 넣을 주소. 배달되지 않는다. */
    public static String placeholderFor(String sub) {
        return sub + RELAY_DOMAIN;
    }

    /** 이 주소가 {@link #placeholderFor} 로 만들어진 것인지 — 즉 <b>보낼 수 없는 주소</b>인지. */
    public static boolean isPlaceholder(String email, String sub) {
        return email != null && sub != null && email.equals(placeholderFor(sub));
    }
}
