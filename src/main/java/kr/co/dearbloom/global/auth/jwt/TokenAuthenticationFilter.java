package kr.co.dearbloom.global.auth.jwt;

import kr.co.dearbloom.global.dto.response.ApiResponse;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import kr.co.dearbloom.global.dto.response.exception.ErrorDetail;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// 이 필터는 액세스 토큰값이 담긴 Authorization 헤더값을 가져온 뒤 액세스 토큰이 유효하다면 인증 정보를 설정한다.
@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {
    private final TokenProvider tokenProvider;
    private final ObjectMapper objectMapper;
    private final static String HEADER_AUTHORIZATION = "Authorization";
    private final static String TOKEN_PREFIX = "Bearer";

    private boolean isSkipPath(String path) {
        // 스킵 예외 경로는 SKIP_TOKEN_PREFIXES 에 걸리더라도 토큰 검증을 수행
        if (PublicPaths.SKIP_TOKEN_EXCEPTIONS.stream().anyMatch(path::startsWith)) {
            return false;
        }
        return PublicPaths.SKIP_TOKEN_PREFIXES.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // 인증 불필요 경로 → 토큰 검증 스킵
        if (isSkipPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 요청 헤더의 Authorization 키의 값 조회
        String authorizationHeader = request.getHeader(HEADER_AUTHORIZATION);

        // 가져온 값에서 접두사 제거
        String token = getAccessToken(authorizationHeader);

        if (token != null) {
            // 토큰이 존재하면 유효성 검사 — 유효하면 인증 설정, 만료/무효하면 401 반환
            if (tokenProvider.validToken(token)) {
                Authentication authentication = tokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                // ApiResponse 객체를 JSON으로 변환하여 응답 본문에 작성
                ErrorCode errorCode = ErrorCode.EXPIRED_TOKEN;
                ApiResponse<?> body = ApiResponse.error(
                        new ErrorDetail(errorCode.getCode(), errorCode.getMessage())
                );
                response.setStatus(errorCode.getHttpStatus().value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(objectMapper.writeValueAsString(body));
                return;
            }
        }

        // 토큰이 없는 경우(비인증 API)는 그대로 통과
        filterChain.doFilter(request, response);
    }


    private String getAccessToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith(TOKEN_PREFIX)){
            return authorizationHeader.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}
