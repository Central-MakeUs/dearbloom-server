package kr.co.dearbloom.domain.notification.entity;

/**
 * 푸시 대상 기기 플랫폼.
 *
 * <p>1차 범위는 iOS 뿐이다. ANDROID 는 값으로만 존재하고, 앱이 Android 에서 토큰을 요청하지 않으므로
 * 실제로 저장되지 않는다. Android 를 켤 때 발송 대상 조회의 IOS 필터만 걷어내면 된다.
 */
public enum DevicePlatform {
    IOS,
    ANDROID
}
