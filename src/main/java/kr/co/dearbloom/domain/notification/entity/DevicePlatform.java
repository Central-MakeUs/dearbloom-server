package kr.co.dearbloom.domain.notification.entity;

/**
 * 푸시 대상 기기 플랫폼.
 *
 * <p>iOS·Android 를 모두 발송한다. 한 회원이 두 기기를 함께 쓸 수 있어 발송 대상 조회는 플랫폼을
 * 가리지 않는다({@code DeviceTokenQueryService#findSendTargets}).
 *
 * <p>이 값은 <b>발송 분기에 쓰이지 않는다.</b> FCM 요청에 apns·android 블록을 함께 실어 보내면
 * FCM 이 대상 토큰의 실제 플랫폼에 맞는 블록만 골라 쓰기 때문이다({@code FcmMessageMapper}).
 * 통계·디버깅용으로만 남긴다.
 */
public enum DevicePlatform {
    IOS,
    ANDROID
}
