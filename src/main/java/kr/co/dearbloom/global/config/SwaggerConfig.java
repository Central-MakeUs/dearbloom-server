package kr.co.dearbloom.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

        return new OpenAPI()
                .info(info)
                .components(new Components()
                        .addSecuritySchemes("Bearer Token", apiKey))
                .addSecurityItem(securityRequirement)
                .addServersItem(new Server().url("/"));
    }
}
