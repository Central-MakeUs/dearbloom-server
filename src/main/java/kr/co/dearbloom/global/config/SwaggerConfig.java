package kr.co.dearbloom.global.config;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import kr.co.dearbloom.global.auth.resolver.CurrentArtist;
import kr.co.dearbloom.global.auth.resolver.CurrentCustomer;
import kr.co.dearbloom.global.auth.resolver.CurrentViewer;
import kr.co.dearbloom.global.swagger.ErrorResponse;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Comparator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Configuration
public class SwaggerConfig {
    static {
        // 리졸버가 토큰으로 주입하는 파라미터들. 등록하지 않으면 springdoc 이 이를 모르고
        // Customer/Artist 엔티티를 query 파라미터로 펼쳐서 문서에 그린다.
        // (@AuthenticationPrincipal 은 springdoc 기본 무시 목록에 이미 있음)
        SpringDocUtils.getConfig().addAnnotationsToIgnore(CurrentArtist.class, CurrentCustomer.class, CurrentViewer.class);
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
                .description("DearBloom " + serverEnvName + " 서버 API 명세서입니다.");

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

    /** 태그 이름 앞머리의 "섹션-순번" 을 뽑는다. 형식에 안 맞으면 정렬상 맨 뒤로 보낸다. */
    private static final Pattern TAG_INDEX = Pattern.compile("^(\\d+)-(\\d+)\\s");

    private static int[] tagIndex(String name) {
        Matcher matcher = TAG_INDEX.matcher(name == null ? "" : name);
        if (!matcher.find()) {
            return new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE};
        }
        return new int[]{Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2))};
    }

    /**
     * 태그 정렬. 태그 이름이 {@code 섹션-순번 [그룹] 리소스} 형식이라 번호가 곧 의도한 노출 순서다.
     * (0 인증 → 1 공통 → 2 고객 → 3 작가 → 9 개발/운영)
     *
     * <p>순서 목록을 따로 두지 않으므로 새 컨트롤러가 추가돼도 등록을 빠뜨려 맨 뒤로 밀리는 일이 없다.
     * 사전순이 아니라 <b>숫자로</b> 비교하므로 한 섹션이 10개를 넘어도("2-10") 자리를 채울 필요가 없다.
     */
    @Bean
    public GlobalOpenApiCustomizer tagOrderCustomizer() {
        return openApi -> {
            if (openApi.getTags() == null) {
                return;
            }
            openApi.getTags().sort(Comparator
                    .comparingInt((Tag tag) -> tagIndex(tag.getName())[0])
                    .thenComparingInt(tag -> tagIndex(tag.getName())[1])
                    .thenComparing(Tag::getName));
        };
    }

    /**
     * Swagger description 에 API 개수 노출.
     * - 실서비스 API: "/dev/", "/health" 경로 접두사와 "9-" (개발/운영 섹션) 태그 제외
     * - Dev API: "/dev/" 경로 접두사
     * - Health API: "/health" 경로 접두사
     */
    @Bean
    public GlobalOpenApiCustomizer apiCountCustomizer() {
        return openApi -> {
            long productionApis = openApi.getPaths().entrySet().stream()
                    .filter(entry -> !entry.getKey().startsWith("/dev/"))
                    .filter(entry -> !entry.getKey().startsWith("/health"))
                    .flatMap(entry -> entry.getValue().readOperations().stream())
                    .filter(op -> op.getTags() == null
                            || op.getTags().stream().noneMatch(t -> t.startsWith("9-")))
                    .count();

            long devApis = openApi.getPaths().entrySet().stream()
                    .filter(entry -> entry.getKey().startsWith("/dev/"))
                    .flatMap(entry -> entry.getValue().readOperations().stream())
                    .count();

            long healthApis = openApi.getPaths().entrySet().stream()
                    .filter(entry -> entry.getKey().startsWith("/health"))
                    .flatMap(entry -> entry.getValue().readOperations().stream())
                    .count();

            String original = openApi.getInfo().getDescription();
            openApi.getInfo().setDescription(
                    (original == null ? "" : original)
                            + String.format(
                                    "<br><br><b>실서비스 API: %d개</b>  /  Dev: %d개  /  Health: %d개",
                                    productionApis, devApis, healthApis));
        };
    }
}
