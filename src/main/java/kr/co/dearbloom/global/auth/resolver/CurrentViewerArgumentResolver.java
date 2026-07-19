package kr.co.dearbloom.global.auth.resolver;

import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.global.auth.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * {@code @CurrentViewer ViewerContext} 를 주입. 비로그인도 허용(guest) 하며 예외를 던지지 않는다.
 * TokenAuthenticationFilter 가 credentials 자리에 심어둔 원본 JWT 로 activeRole/activeProfileId 를 읽는다.
 */
@Component
@RequiredArgsConstructor
public class CurrentViewerArgumentResolver implements HandlerMethodArgumentResolver {
    private final TokenProvider tokenProvider;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentViewer.class)
                && ViewerContext.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                   NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Member member)) {
            return ViewerContext.guest();
        }
        if (!(authentication.getCredentials() instanceof String token) || token.isBlank()) {
            return new ViewerContext(member, null, null);
        }
        return new ViewerContext(member, tokenProvider.getActiveRole(token), tokenProvider.getActiveProfileId(token));
    }
}
