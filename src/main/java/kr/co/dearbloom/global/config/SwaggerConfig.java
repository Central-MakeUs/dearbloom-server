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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class SwaggerConfig {
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
                .title("DearBloom" + " API 명세서")
                .description("DearBloom API 명세서입니다.");

        Components components = new Components()
                .addSecuritySchemes("Bearer Token", apiKey);
        // ApiErrorCodesCustomizer 가 $ref 로 참조하는 공통 에러 스키마를 항상 등록
        // (애노테이션으로 직접 참조되지 않아도 components 에 존재하도록)
        Map<String, Schema> errorSchemas = ModelConverters.getInstance().readAll(ErrorResponse.class);
        errorSchemas.forEach(components::addSchemas);

        return new OpenAPI()
                .info(info)
                .components(components)
                .addSecurityItem(securityRequirement)
                .addServersItem(new Server().url("/"));
    }
}
