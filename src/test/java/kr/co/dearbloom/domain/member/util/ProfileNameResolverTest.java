package kr.co.dearbloom.domain.member.util;

import kr.co.dearbloom.global.validation.ValidationPatterns;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** [임시] 온보딩 이름·닉네임 자동 생성 — 이메일 @ 앞을 잘라 만들되 검증 규칙을 벗어나지 않아야 한다. */
class ProfileNameResolverTest {

    @Test
    void 고객_이름은_한글영문만_남겨_5자로_자른다() {
        assertThat(ProfileNameResolver.customerName("chyun5197@gmail.com")).isEqualTo("chyun");
        assertThat(ProfileNameResolver.customerName("hong.gildong@x.com")).isEqualTo("hongg");
        assertThat(ProfileNameResolver.customerName("김디어@x.com")).isEqualTo("김디어");
    }

    @Test
    void 작가_닉네임은_한글영문숫자밑줄만_남겨_12자로_자른다() {
        assertThat(ProfileNameResolver.artistNickname("chyun5197@gmail.com")).isEqualTo("chyun5197");
        assertThat(ProfileNameResolver.artistNickname("dear_bloom_studio@x.com")).isEqualTo("dear_bloom_s");
        assertThat(ProfileNameResolver.artistNickname("hong.gildong@x.com")).isEqualTo("honggildong");
    }

    @Test
    void 남는_글자가_2자_미만이면_기본값으로_대체한다() {
        assertThat(ProfileNameResolver.customerName("123456@x.com")).isEqualTo("고객");   // 숫자는 실명에 못 쓴다
        assertThat(ProfileNameResolver.customerName("a@x.com")).isEqualTo("고객");        // 1자
        assertThat(ProfileNameResolver.artistNickname("...@x.com")).isEqualTo("작가");
        assertThat(ProfileNameResolver.artistNickname("1@x.com")).isEqualTo("작가");
    }

    @Test
    void 이메일이_없거나_형식이_이상해도_터지지_않는다() {
        assertThat(ProfileNameResolver.customerName(null)).isEqualTo("고객");
        assertThat(ProfileNameResolver.customerName("")).isEqualTo("고객");
        assertThat(ProfileNameResolver.artistNickname("@x.com")).isEqualTo("작가");       // 로컬 파트 없음
        assertThat(ProfileNameResolver.artistNickname("noatsign")).isEqualTo("noatsign"); // @ 자체가 없음
    }

    @Test
    void 생성된_값은_실제_검증_규칙을_통과한다() {
        String[] emails = {
                "chyun5197@gmail.com", "hong.gildong@x.com", "김디어@x.com",
                "123456@x.com", "a@x.com", "dear_bloom_studio@x.com", "@x.com"
        };
        for (String email : emails) {
            assertThat(ValidationPatterns.REAL_NAME.matcher(ProfileNameResolver.customerName(email)).matches())
                    .as("고객 이름 규칙 위반: %s", email).isTrue();
            assertThat(ValidationPatterns.NICKNAME.matcher(ProfileNameResolver.artistNickname(email)).matches())
                    .as("작가 닉네임 규칙 위반: %s", email).isTrue();
        }
    }
}
