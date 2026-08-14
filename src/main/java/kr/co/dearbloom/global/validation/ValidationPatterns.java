package kr.co.dearbloom.global.validation;

import java.util.regex.Pattern;

public class ValidationPatterns {
    /**
     * 작가 닉네임. 2~12자의 한글·영문·숫자·{@code _} 에 <b>단어 사이 공백</b>까지 허용한다("블루밍데이즈 스냅").
     * 앞뒤 공백과 연속 공백은 막는다 — 화면에서 어색하게 보이고, 눈에 안 보이는 차이로 중복 닉네임이 생긴다.
     * (선행 lookahead 가 전체 길이 2~12자를 보고, 뒤쪽이 "단어(공백 단어)*" 형태를 강제한다)
     */
    public static final Pattern NICKNAME =
            Pattern.compile("^(?=.{2,12}$)[가-힣a-zA-Z0-9_]+(?: [가-힣a-zA-Z0-9_]+)*$");

    // 고객 실명. 2~12자의 한글 또는 영문 허용(공백·숫자 불가).
    // 상한이 12자인 건 온보딩에서 소셜 이름을 12자로 잘라 채우기 때문 — 그보다 짧으면 자동 생성된
    // 이름이 프로필 수정에서 반려된다(ProfileNameResolver 의 MIN/MAX 와 맞춰야 함).
    public static final Pattern REAL_NAME =
            Pattern.compile("^[가-힣a-zA-Z]{2,12}$");

    private ValidationPatterns() {}
}
