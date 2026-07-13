package kr.co.dearbloom.global.auth.resolver;

import kr.co.dearbloom.global.dto.response.exception.CustomException;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * {@link CurrentCustomerArgumentResolver}/{@link CurrentArtistArgumentResolver} 공용.
 * TokenAuthenticationFilter 가 {@code UsernamePasswordAuthenticationToken(member, token, authorities)} 로
 * credentials 자리에 원본 JWT 문자열을 이미 심어둔 것을 재사용 — 토큰을 다시 파싱할 필요 없음.
 */
abstract class CurrentProfileTokenSupport {

    protected String extractToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getCredentials() instanceof String token) || token.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        return token;
    }
}
