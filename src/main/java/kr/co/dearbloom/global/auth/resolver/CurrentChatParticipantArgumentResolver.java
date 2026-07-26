package kr.co.dearbloom.global.auth.resolver;

import kr.co.dearbloom.domain.chat.dto.ChatParticipant;
import kr.co.dearbloom.domain.member.entity.MemberRole;
import kr.co.dearbloom.global.auth.jwt.TokenProvider;
import kr.co.dearbloom.global.dto.response.exception.CustomException;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * {@code @CurrentChatParticipant ChatParticipant me} 에 토큰의 (activeRole, activeProfileId) 를 주입한다.
 * activeRole·profileId 가 없으면(온보딩 전 등) 403(ROLE_ACCESS_DENIED).
 */
@Component
@RequiredArgsConstructor
public class CurrentChatParticipantArgumentResolver extends CurrentProfileTokenSupport
        implements HandlerMethodArgumentResolver {
    private final TokenProvider tokenProvider;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentChatParticipant.class)
                && ChatParticipant.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        String token = extractToken();
        MemberRole role = tokenProvider.getActiveRole(token);
        Long profileId = tokenProvider.getActiveProfileId(token);
        if (role == null || profileId == null) {
            throw new CustomException(ErrorCode.ROLE_ACCESS_DENIED);
        }
        return new ChatParticipant(role, profileId);
    }
}
