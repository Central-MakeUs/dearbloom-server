package kr.co.dearbloom.global.validation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** 작가 닉네임 패턴 — 단어 사이 공백은 허용하되 앞뒤·연속 공백은 막는다. */
class NicknamePatternTest {

    private boolean valid(String value) {
        return ValidationPatterns.NICKNAME.matcher(value).matches();
    }

    @Test
    void 단어_사이_공백을_허용한다() {
        assertThat(valid("블루밍데이즈 스냅")).isTrue();
        assertThat(valid("오후 세시")).isTrue();
        assertThat(valid("a b")).isTrue();
        assertThat(valid("스냅 A 1")).isTrue(); // 공백이 여러 번이어도 각각 한 칸이면 허용
    }

    @Test
    void 공백_없는_기존_닉네임은_그대로_유효하다() {
        assertThat(valid("블룸작가")).isTrue();
        assertThat(valid("dear_bloom")).isTrue();
        assertThat(valid("작가1")).isTrue();
    }

    @Test
    void 앞뒤_공백은_거부한다() {
        assertThat(valid(" 블룸작가")).isFalse();
        assertThat(valid("블룸작가 ")).isFalse();
        assertThat(valid(" 블룸 작가 ")).isFalse();
    }

    @Test
    void 연속_공백은_거부한다() {
        assertThat(valid("블룸  작가")).isFalse();
        assertThat(valid("a   b")).isFalse();
    }

    @Test
    void 공백만으로는_만들_수_없다() {
        assertThat(valid("  ")).isFalse();
        assertThat(valid(" ")).isFalse();
    }

    @Test
    void 길이는_공백을_포함해_2에서_12자다() {
        assertThat(valid("가")).isFalse();                 // 1자
        assertThat(valid("가 나")).isTrue();                // 3자(공백 포함)
        assertThat(valid("가나다라마바사아자차카타")).isTrue();    // 12자
        assertThat(valid("가나다라마바사아자차카타파")).isFalse();  // 13자
        assertThat(valid("가나다라마바사아자차 카타")).isFalse();   // 공백 포함 13자
    }

    @Test
    void 허용되지_않는_문자는_거부한다() {
        assertThat(valid("블룸-작가")).isFalse();
        assertThat(valid("블룸@작가")).isFalse();
        assertThat(valid("블룸\t작가")).isFalse(); // 탭은 공백으로 인정하지 않는다
    }
}
