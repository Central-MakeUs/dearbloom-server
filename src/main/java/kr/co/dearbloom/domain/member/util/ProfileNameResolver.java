package kr.co.dearbloom.domain.member.util;

import java.util.regex.Pattern;

/**
 * 온보딩에서 이름·닉네임을 입력받지 않는 <b>임시 정책</b>용. 소셜 계정 이메일의
 * 로컬 파트(@ 앞)를 잘라 고객 실명 / 작가 닉네임을 만든다.
 * <p>
 * 그냥 자르기만 하면 검증 규칙을 벗어난 값이 저장되고, 그러면 사용자가 <b>자기 이름 때문에
 * 프로필 수정을 저장하지 못하는</b> 상태가 된다. 그래서 다음 순서로 보정한다.
 * <ol>
 *   <li>@ 앞만 남긴다</li>
 *   <li>허용하지 않는 문자를 버린다 — 고객은 한글·영문만, 작가는 한글·영문·숫자·{@code _}
 *       ({@code .} {@code -} {@code +} 처럼 이메일에 흔한 문자가 그대로 남으면 검증에 걸린다)</li>
 *   <li>최대 길이로 자른다 (고객 5자 / 작가 12자)</li>
 *   <li>남은 길이가 2자 미만이면 기본값으로 대체한다</li>
 * </ol>
 * 길이·허용 문자는 {@code ValidationPatterns.REAL_NAME} / {@code NICKNAME} 과 맞춰야 한다.
 */
public final class ProfileNameResolver {
    /** 고객 실명 최대 길이. REAL_NAME(2~5자)과 맞춘다. */
    public static final int CUSTOMER_NAME_MAX_LENGTH = 5;
    /** 작가 닉네임 최대 길이. NICKNAME(2~12자)과 맞춘다. */
    public static final int ARTIST_NICKNAME_MAX_LENGTH = 12;
    /** 두 규칙 모두 최소 2자다. */
    private static final int MIN_LENGTH = 2;

    // 각 규칙이 허용하지 않는 문자(버릴 대상)
    private static final Pattern CUSTOMER_DISALLOWED = Pattern.compile("[^가-힣a-zA-Z]");
    private static final Pattern ARTIST_DISALLOWED = Pattern.compile("[^가-힣a-zA-Z0-9_]");

    private static final String CUSTOMER_FALLBACK = "고객";
    private static final String ARTIST_FALLBACK = "작가";

    private ProfileNameResolver() {}

    /** 고객 실명. 예) chyun5197@gmail.com → "chyun" */
    public static String customerName(String email) {
        return resolve(email, CUSTOMER_DISALLOWED, CUSTOMER_NAME_MAX_LENGTH, CUSTOMER_FALLBACK);
    }

    /** 작가 닉네임. 예) chyun5197@gmail.com → "chyun5197" (중복 회피는 호출부 책임) */
    public static String artistNickname(String email) {
        return resolve(email, ARTIST_DISALLOWED, ARTIST_NICKNAME_MAX_LENGTH, ARTIST_FALLBACK);
    }

    private static String resolve(String email, Pattern disallowed, int maxLength, String fallback) {
        String cleaned = disallowed.matcher(localPart(email)).replaceAll("");
        if (cleaned.length() < MIN_LENGTH) {
            return fallback; // 숫자만 있는 이메일 등, 남는 글자가 없는 경우
        }
        return cleaned.length() <= maxLength ? cleaned : cleaned.substring(0, maxLength);
    }

    private static String localPart(String email) {
        if (email == null || email.isBlank()) {
            return "";
        }
        String trimmed = email.trim();
        int atIndex = trimmed.indexOf('@');
        return atIndex >= 0 ? trimmed.substring(0, atIndex) : trimmed;
    }
}
