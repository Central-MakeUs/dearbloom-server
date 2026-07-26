package kr.co.dearbloom.global.config;

import kr.co.dearbloom.global.auth.resolver.CurrentArtistArgumentResolver;
import kr.co.dearbloom.global.auth.resolver.CurrentChatParticipantArgumentResolver;
import kr.co.dearbloom.global.auth.resolver.CurrentCustomerArgumentResolver;
import kr.co.dearbloom.global.auth.resolver.CurrentViewerArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * {@code @CurrentCustomer}/{@code @CurrentArtist} 커스텀 파라미터 애노테이션을 Spring MVC 에 등록
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    private final CurrentCustomerArgumentResolver currentCustomerArgumentResolver;
    private final CurrentArtistArgumentResolver currentArtistArgumentResolver;
    private final CurrentViewerArgumentResolver currentViewerArgumentResolver;
    private final CurrentChatParticipantArgumentResolver currentChatParticipantArgumentResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentCustomerArgumentResolver);
        resolvers.add(currentArtistArgumentResolver);
        resolvers.add(currentViewerArgumentResolver);
        resolvers.add(currentChatParticipantArgumentResolver);
    }
}
