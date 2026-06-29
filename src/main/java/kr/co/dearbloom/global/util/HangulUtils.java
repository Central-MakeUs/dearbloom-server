package kr.co.dearbloom.global.util;

public final class HangulUtils {
    private static final char HANGUL_BASE = 0xAC00; // '가'
    private static final char HANGUL_END = 0xD7A3;  // '힣'
    private static final int CHOSUNG_BLOCK = 588;   // 21(중성) * 28(종성)

    // 완성형 한글 초성 추출용 호환 자모 테이블 (인덱스 = (음절-가)/588)
    private static final char[] CHOSUNG = {
            'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ',
            'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    };

    private HangulUtils() {}

    /** 완성형 한글이면 초성(호환 자모)을, 아니면 입력 문자를 그대로 반환. */
    public static char getChosung(char c) {
        if (c >= HANGUL_BASE && c <= HANGUL_END) {
            return CHOSUNG[(c - HANGUL_BASE) / CHOSUNG_BLOCK];
        }
        return c;
    }

    /** 호환 자모 자음(ㄱ~ㅎ)인지. 모음(ㅏ~)은 제외. */
    public static boolean isConsonant(char c) {
        return c >= 'ㄱ' && c <= 'ㅎ';
    }

    /**
     * query 를 prefix 로 보고 target 과 글자 단위로 매칭.
     * - query 글자가 자음이면 target 글자의 초성과 비교
     * - 그 외에는 글자 그대로 비교
     */
    public static boolean matchesPrefix(String target, String query) {
        if (query.length() > target.length()) {
            return false;
        }
        for (int i = 0; i < query.length(); i++) {
            char q = query.charAt(i);
            char t = target.charAt(i);
            if (isConsonant(q)) {
                if (getChosung(t) != q) {
                    return false;
                }
            } else if (t != q) {
                return false;
            }
        }
        return true;
    }

    /** 앞에서부터 자음이 나오기 전까지의 완성형 구간 (Redis ZRANGEBYLEX 후보 축소용). */
    public static String leadingCompletePrefix(String query) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < query.length(); i++) {
            char c = query.charAt(i);
            if (isConsonant(c)) {
                break;
            }
            sb.append(c);
        }
        return sb.toString();
    }
}
