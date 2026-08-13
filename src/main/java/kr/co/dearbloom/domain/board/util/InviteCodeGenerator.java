package kr.co.dearbloom.domain.board.util;

import java.security.SecureRandom;

/**
 * 공동보드 초대 코드 생성기.
 * <p>
 * 카톡 등으로 전달되는 URL 에 그대로 실리므로 <b>추측이 불가능해야</b> 한다.
 * 대문자·숫자에서 혼동 문자(0 O 1 I L)를 뺀 31자 중 6자 → 약 8.9억 가지.
 * 숫자 6자리(100만)로는 활성 보드가 쌓였을 때 무작위 대입으로 남의 보드가 열린다.
 */
public final class InviteCodeGenerator {
    /** 혼동 문자(0 O 1 I L) 제외 — 사용자가 눈으로 옮겨 적을 수도 있어서. */
    private static final char[] ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789".toCharArray();
    private static final int CODE_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private InviteCodeGenerator() {}

    public static String generate() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(ALPHABET[RANDOM.nextInt(ALPHABET.length)]);
        }
        return sb.toString();
    }
}
