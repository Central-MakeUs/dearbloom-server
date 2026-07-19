package kr.co.dearbloom.global.auth.resolver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 비로그인/고객/작가를 모두 허용하는 조회용 뷰어 컨텍스트를 주입한다.
 * {@code @CurrentArtist}/{@code @CurrentCustomer} 와 달리 role 불일치로 403 을 던지지 않는다.
 * 토큰이 없으면 guest 로, 있으면 토큰의 activeRole/activeProfileId 를 담아 준다.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentViewer {
}
