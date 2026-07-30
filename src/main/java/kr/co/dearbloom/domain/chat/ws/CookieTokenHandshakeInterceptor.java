package kr.co.dearbloom.domain.chat.ws;

import jakarta.servlet.http.Cookie;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * 핸드셰이크(HTTP) 단계에서 accessToken 쿠키를 꺼내 WebSocket 세션 속성에 넘긴다.
 * <p>
 * 웹 프론트는 토큰을 httpOnly 쿠키로 들고 있어 JS 로 읽을 수 없다 → STOMP CONNECT 프레임에
 * Authorization 헤더를 실을 수 없다. 반면 {@code /ws} 핸드셰이크는 일반 HTTP 요청이라 브라우저가
 * 쿠키를 자동으로 붙여 보내므로, 여기서 받아 두고 {@link StompAuthChannelInterceptor} 가 fallback 으로 쓴다.
 * 네이티브 앱은 CONNECT 헤더에 직접 토큰을 넣을 수 있어 이 경로를 타지 않는다.
 */
@Component
public class CookieTokenHandshakeInterceptor implements HandshakeInterceptor {
    /** 세션 속성 키. {@link StompAuthChannelInterceptor} 가 같은 키로 읽는다. */
    static final String ACCESS_TOKEN_ATTRIBUTE = "accessToken";

    private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        // 토큰이 없어도 핸드셰이크는 통과시킨다 — 인증 실패는 CONNECT 단계에서 판정한다.
        if (request instanceof ServletServerHttpRequest servletRequest) {
            Cookie[] cookies = servletRequest.getServletRequest().getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (ACCESS_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                        attributes.put(ACCESS_TOKEN_ATTRIBUTE, cookie.getValue());
                        break;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // no-op
    }
}
