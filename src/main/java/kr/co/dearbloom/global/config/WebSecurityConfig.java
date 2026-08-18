package kr.co.dearbloom.global.config;

import kr.co.dearbloom.domain.auth.service.AuthService;
import kr.co.dearbloom.domain.auth.service.OAuthOneTimeCodeService;
import kr.co.dearbloom.global.auth.jwt.PublicPaths;
import kr.co.dearbloom.global.auth.jwt.TokenAuthenticationFilter;
import kr.co.dearbloom.global.auth.jwt.TokenProvider;
import kr.co.dearbloom.global.auth.oauth.OAuth2AuthorizationRequestBasedOnCookieRepository;
import kr.co.dearbloom.global.auth.oauth.OAuth2SuccessHandler;
import kr.co.dearbloom.global.auth.oauth.OAuth2UserCustomService;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {
    private final TokenProvider tokenProvider;
    private final ObjectMapper objectMapper;
    private final OAuth2UserCustomService oAuth2UserCustomService;
    private final AuthService authService;
    private final OAuthOneTimeCodeService oAuthOneTimeCodeService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable) // 클라이언트 측에서 로그아웃을 처리하는 대신에 인증 서버에 로그아웃 요청을 전달하여 세션을 종료하고 토큰을 무효화한다.
                .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        // SSE(SseEmitter) 등 async 응답의 ASYNC 재디스패치는 인증 재검증에서 제외.
                        // 최초 REQUEST에서 이미 인증 완료 → ASYNC는 응답 생성 단계라 재검증 불필요.
                        .dispatcherTypeMatchers(DispatcherType.ASYNC).permitAll()
                        // CORS preflight 는 Authorization 헤더 없이 온다. CORS 설정이 MVC 레이어(CorsConfig)라
                        // 시큐리티 체인 뒤에서 동작하므로, 여기서 열어주지 않으면 preflight 가 401 로 막힌다.
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(PublicPaths.permitAllPatterns()).permitAll()
                        // 조회만 공개. 같은 prefix 의 쓰기(작품 등록·삭제, 보드 입장)는 아래 authenticated 로 떨어진다.
                        .requestMatchers(HttpMethod.GET, PublicPaths.publicGetPatterns()).permitAll()
                        .requestMatchers("/api/**").authenticated()
                        // /ws(웹소켓), /oauth2/**(소셜 로그인 리다이렉트) 등 API 밖 경로는 그대로 통과시킨다.
                        .anyRequest().permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                                // Authorization 요청과 관련된 상태 저장 | (url 기본값: /oauth2/authorization/{registrationId})
                                .authorizationEndpoint(authorizationEndpoint
                                        -> authorizationEndpoint.authorizationRequestRepository(oAuth2AuthorizationRequestBasedOnCookieRepository()))
                                .userInfoEndpoint(userInfoEndpoint
                                        -> userInfoEndpoint.userService(oAuth2UserCustomService))
                                .successHandler(oAuth2SuccessHandler())
//                        .successHandler(oAuth2SuccessHandler)
                )
                .logout(logout -> logout // 로그아웃 설정
//                        .logoutSuccessUrl("/") // 로그아웃 시 이동 URL
                                .invalidateHttpSession(true) // 로그아웃 이후 세션을 전체 삭제할지 여부
                )
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
//                                new AntPathRequestMatcher("/api/**")
                                PathPatternRequestMatcher.withDefaults().matcher("/api/**")
                        )
                )
                .build();
    }

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter(tokenProvider, objectMapper);
    }

    @Bean
    public OAuth2AuthorizationRequestBasedOnCookieRepository oAuth2AuthorizationRequestBasedOnCookieRepository() {
        return new OAuth2AuthorizationRequestBasedOnCookieRepository();
    }

    @Bean
    public OAuth2SuccessHandler oAuth2SuccessHandler() {
        return new OAuth2SuccessHandler(
                authService,
                oAuth2AuthorizationRequestBasedOnCookieRepository(),
                oAuthOneTimeCodeService
        );
    }
}
