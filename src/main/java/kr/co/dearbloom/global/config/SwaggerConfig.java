package kr.co.dearbloom.global.config;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import kr.co.dearbloom.global.auth.resolver.CurrentArtist;
import kr.co.dearbloom.global.auth.resolver.CurrentCustomer;
import kr.co.dearbloom.global.swagger.ErrorResponse;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Configuration
public class SwaggerConfig {
    static {
        // 리졸버가 토큰으로 주입하는 파라미터들. 등록하지 않으면 springdoc 이 이를 모르고
        // Customer/Artist 엔티티를 query 파라미터로 펼쳐서 문서에 그린다.
        // (@AuthenticationPrincipal 은 springdoc 기본 무시 목록에 이미 있음)
        SpringDocUtils.getConfig().addAnnotationsToIgnore(CurrentArtist.class, CurrentCustomer.class);
    }

    // 현재 서버 환경명 (예: "로컬", "개발", "운영"). Swagger 제목에 노출.
    @Value("${app.server-env-name}")
    private String serverEnvName;

    @Bean
    public OpenAPI openAPI() {
        SecurityScheme apiKey = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .in(SecurityScheme.In.HEADER)
                .name("Authorization")
                .scheme("bearer")
                .bearerFormat("JWT");
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("Bearer Token");

        Info info = new Info()
                .title("DearBloom " + serverEnvName + " 서버 API 명세서")
                .description("DearBloom " + serverEnvName + " 서버 API 명세서입니다."
                        + "<br><br>⚠️ <b>개발 편의성: 인가(접근 제어)가 열려 있습니다.</b> "
                        + "<br>인증 필터는 동작합니다 — 토큰이 있으면 검증해 사용자 정보를 채우고, 없어도 통과합니다. "
                        + "<br>개발이 어느 정도 진행되면 인가(authenticated)를 활성화할 예정입니다.");

        Components components = new Components()
                .addSecuritySchemes("Bearer Token", apiKey);
        // ApiErrorCodesCustomizer 가 $ref 로 참조하는 공통 에러 스키마를 항상 등록
        // (어노테이션으로 직접 참조되지 않아도 components 에 존재하도록)
        Map<String, Schema> errorSchemas = ModelConverters.getInstance().readAll(ErrorResponse.class);
        errorSchemas.forEach(components::addSchemas);

        return new OpenAPI()
                .info(info)
                .components(components)
                .addSecurityItem(securityRequirement)
                .addServersItem(new Server().url("/"));
    }

    private static final List<String> TAG_ORDER = List.of(
            "Social Login",
            "Social Login - Local Web",
            "Auth",
            "Member",
            "Customer",
            "Saved Artwork",
            "Artist",
            "Artwork",
            "University",
            "File",
            "Dev - Member",
            "Dev - Response",
            "Dev - Infra",
            "Health - Spring",
            "Health - Infra"
    );

    @Bean
    public GlobalOpenApiCustomizer tagOrderCustomizer() {
        return openApi -> {
            if (openApi.getTags() == null) {
                return;
            }
            openApi.getTags().sort(Comparator.comparingInt(tag -> {
                int idx = TAG_ORDER.indexOf(tag.getName());
                return idx < 0 ? Integer.MAX_VALUE : idx;
            }));
        };
    }
}
