package kr.co.dearbloom.global.auth.resolver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 컨트롤러 파라미터에 붙이면 현재 요청의 activeRole 이 CUSTOMER 인지 검증한 뒤
 * activeProfileId 로 조회한 Customer 엔티티를 주입한다. activeRole 불일치 시 403.
 * {@link CurrentCustomerArgumentResolver} 가 실제 처리를 담당.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentCustomer {
}
