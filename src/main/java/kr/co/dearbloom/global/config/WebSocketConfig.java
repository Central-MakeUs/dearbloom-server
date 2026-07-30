package kr.co.dearbloom.global.config;

import kr.co.dearbloom.domain.chat.ws.CookieTokenHandshakeInterceptor;
import kr.co.dearbloom.domain.chat.ws.StompAuthChannelInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * STOMP over WebSocket 설정.
 * - 핸드셰이크 엔드포인트: {@code /ws}
 * - 구독(수신): {@code /topic/rooms/{roomId}} — 전송은 REST(POST)로 저장 후 서버가 이 토픽으로 브로드캐스트한다.
 * - 인증/인가는 {@link StompAuthChannelInterceptor}(CONNECT 토큰 검증, SUBSCRIBE 참여자 검증).
 * - 웹은 토큰이 httpOnly 쿠키라 헤더에 실을 수 없어, 핸드셰이크에서 쿠키를 받아둔다({@link CookieTokenHandshakeInterceptor}).
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final StompAuthChannelInterceptor stompAuthChannelInterceptor;
    private final CookieTokenHandshakeInterceptor cookieTokenHandshakeInterceptor;

    // REST(CorsConfig)와 같은 목록을 쓴다. 쿠키로 인증하므로 와일드카드를 두면 임의 사이트가
    // 사용자 쿠키를 실어 붙을 수 있어(CSRF) 반드시 실제 프론트 origin 으로 제한한다.
    @Value("${url.cors-origins}")
    private String[] corsOrigins;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins(corsOrigins)
                .addInterceptors(cookieTokenHandshakeInterceptor);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompAuthChannelInterceptor);
    }
}
