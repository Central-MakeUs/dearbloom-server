package kr.co.dearbloom.global.auth.resolver;

import kr.co.dearbloom.domain.artist.entity.artist.Artist;
import kr.co.dearbloom.domain.artist.repository.ArtistRepository;
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
 * {@code @CurrentArtist Artist artist} 파라미터에 activeProfileId 로 조회한 Artist 를 주입한다.
 * activeRole 이 ARTIST 가 아니면 403(ROLE_ACCESS_DENIED).
 */
@Component
@RequiredArgsConstructor
public class CurrentArtistArgumentResolver extends CurrentProfileTokenSupport implements HandlerMethodArgumentResolver {
    private final TokenProvider tokenProvider;
    private final ArtistRepository artistRepository;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentArtist.class)
                && Artist.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                   NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        String token = extractToken();

        if (tokenProvider.getActiveRole(token) != MemberRole.ARTIST) {
            throw new CustomException(ErrorCode.ROLE_ACCESS_DENIED);
        }

        Long activeProfileId = tokenProvider.getActiveProfileId(token);
        if (activeProfileId == null) {
            throw new CustomException(ErrorCode.ROLE_ACCESS_DENIED);
        }

        return artistRepository.findById(activeProfileId)
                .orElseThrow(() -> new CustomException(ErrorCode.ARTIST_NOT_FOUND));
    }
}
