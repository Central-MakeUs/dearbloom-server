package kr.co.dearbloom.global.config;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import kr.co.dearbloom.global.swagger.ErrorResponse;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Configuration
public class SwaggerConfig {
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
                        + "<br><br>⚠️ <b>개발 편의성: 인증이 비활성화되어 있습니다.</b> "
                        + "<br>현재 토큰 없이 모든 API 호출 가능 (토큰이 있으면 검증하지만, 없어도 통과) "
                        + "<br>개발이 어느 정도 진행되면 인증을 활성화할 예정입니다.");

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

    // 태그 노출 순서. springdoc 이 컨트롤러에서 태그를 스캔·머지한 "뒤"에 실행되어 최종 순서를 강제한다.
    // (OpenAPI 빈의 .tags() 로는 머지 과정에서 순서가 덮여 안 먹는 경우가 있어 customizer 로 처리)
    // 여기 나열한 순서대로 표시되며, 목록에 없는 태그는 뒤로 밀린다. description 은 각 컨트롤러 @Tag 값 유지.
    private static final List<String> TAG_ORDER = List.of(
            "Auth",
            "OAuth - Local Entry",
            "Member",
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
