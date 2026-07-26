package kr.co.dearbloom.global.auth.resolver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 채팅 API 파라미터에 현재 토큰의 (activeRole, profileId) 를 담은 ChatParticipant 를 주입. */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentChatParticipant {
}
