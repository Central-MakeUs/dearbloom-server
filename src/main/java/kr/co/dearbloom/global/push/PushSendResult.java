package kr.co.dearbloom.global.push;

/**
 * 푸시 1건의 발송 결과.
 *
 * <p>호출측은 {@link #shouldDeleteToken()} 이 true 면 그 토큰을 그 자리에서 지운다.
 * FCM 은 죽은 토큰을 응답으로 바로 알려주므로, 별도의 receipt 폴링 스케줄러가 필요 없다.
 */
public enum PushSendResult {
    SUCCESS,

    /** 토큰이 더 이상 유효하지 않음(앱 삭제·재설치, 형식 오류) → 즉시 삭제 대상. */
    TOKEN_INVALID,

    /** 일시 오류(네트워크·5xx·429). 토큰은 유지한다. */
    RETRYABLE_FAILURE,

    /** 그 외 실패. 토큰은 유지한다. */
    FAILURE;

    public boolean shouldDeleteToken() {
        return this == TOKEN_INVALID;
    }
}
