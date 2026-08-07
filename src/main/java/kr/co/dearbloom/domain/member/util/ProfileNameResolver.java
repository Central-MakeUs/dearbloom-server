package kr.co.dearbloom.domain.member.util;

import kr.co.dearbloom.domain.member.entity.Member;

/**
 * 소셜 계정 이름(Member.name)에서 프로필 이름(고객 실명 / 작가 닉네임)을 만든다.
 * <p>
 * 온보딩에서 이름을 따로 입력받지 않는 임시 정책이라, 소셜에서 받아둔 값을 그대로 쓴다.
 * 사용자는 프로필 수정 API 로 바꿀 수 있다.
 * <p>
 * 소셜 이름은 형식·길이가 제각각이라 그대로 넣으면 검증 규칙(2~12자)을 벗어날 수 있고, 그러면
 * 사용자가 <b>자기 이름 때문에 프로필 수정을 저장하지 못하는</b> 상태가 된다. 그래서 다음 순서로 보정한다.
 * <ol>
 *   <li>값이 없으면 {@code noname}</li>
 *   <li>이메일이면 {@code @} 앞(로컬 파트)만 남긴다 — 길이 자르기보다 <b>먼저</b> 해야 한다</li>
 *   <li>외자면 뒤에 {@code 아무개} 를 붙여 최소 길이를 맞춘다 ("김" → "김아무개")</li>
 *   <li>12자를 넘으면 앞 12자로 자른다</li>
 * </ol>
 */
public final class ProfileNameResolver {
    /** 프로필 이름 길이. 고객 실명·작가 닉네임 검증 규칙(2~12자)과 같아야 한다. */
    public static final int PROFILE_NAME_MIN_LENGTH = 2;
    public static final int PROFILE_NAME_MAX_LENGTH = 12;

    private static final String DEFAULT_PROFILE_NAME = "noname";
    /** 외자 이름 보정용 접미사. "김" → "김아무개" */
    private static final String SHORT_NAME_SUFFIX = "아무개";

    private ProfileNameResolver() {}

    public static String resolve(Member member) {
        String name = member.getName();
        if (name == null || name.isBlank()) {
            return DEFAULT_PROFILE_NAME;
        }
        String resolved = name.trim();

        // 소셜에 따라 name 자리에 이메일이 들어온다(예: Apple 은 identityToken 에 이름이 없어 이메일을 넣어둔다).
        // 길이부터 자르면 "abc123@priva" 같은 값이 남으므로 로컬 파트 분리를 먼저 한다.
        int atIndex = resolved.indexOf('@');
        if (atIndex >= 0) {
            resolved = resolved.substring(0, atIndex).trim();
        }
        if (resolved.isEmpty()) {
            return DEFAULT_PROFILE_NAME; // "@example.com" 처럼 로컬 파트가 없는 경우
        }

        if (resolved.length() < PROFILE_NAME_MIN_LENGTH) {
            return resolved + SHORT_NAME_SUFFIX;
        }
        return resolved.length() <= PROFILE_NAME_MAX_LENGTH
                ? resolved
                : resolved.substring(0, PROFILE_NAME_MAX_LENGTH);
    }
}
