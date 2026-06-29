//package kr.co.dearbloom.global.config;
//
//import io.micrometer.observation.ObservationPredicate;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.server.observation.ServerRequestObservationContext;
//
//@Configuration
//public class ObservationConfig {
//
//    /**
//     * 관측 가치가 낮거나 호출량이 많은 경로의 Trace 생성 자체를 차단.
//     * - /health : docker healthcheck
//     * - /api/universities/search : 자동완성 (타이핑마다 호출 → T race 폭증 방지)
//     */
//    @Bean
//    ObservationPredicate noiseObservationFilter() {
//        return (name, context) -> {
//            if (context instanceof ServerRequestObservationContext ctx) {
//                String uri = ctx.getCarrier().getRequestURI();
//                return !(uri.startsWith("/health")
//                        || uri.startsWith("/api/universities/search"));
//            }
//            return true;
//        };
//    }
//}
