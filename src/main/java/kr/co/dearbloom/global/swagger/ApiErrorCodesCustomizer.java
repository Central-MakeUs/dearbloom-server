package kr.co.dearbloom.global.swagger;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * {@link ApiErrorCodes} 로 나열된 에러코드를 HTTP 상태코드별로 그룹핑하여
 * Swagger 응답을 자동 생성한다. 같은 상태코드에 여러 코드가 있으면 예시 드롭다운으로 표시된다.
 * (springdoc 이 OperationCustomizer 빈을 자동 등록)
 */
@Component
public class ApiErrorCodesCustomizer implements OperationCustomizer {

    private static final String SCHEMA_REF = "#/components/schemas/ErrorResponse";

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        ApiErrorCodes annotation = handlerMethod.getMethodAnnotation(ApiErrorCodes.class);
        if (annotation == null || annotation.value().length == 0) {
            return operation;
        }

        // 상태코드별로 에러코드 그룹핑 (선언 순서 유지)
        Map<HttpStatus, List<ErrorCode>> grouped = Arrays.stream(annotation.value())
                .collect(Collectors.groupingBy(ErrorCode::getHttpStatus, LinkedHashMap::new, Collectors.toList()));

        ApiResponses responses = operation.getResponses();
        grouped.forEach((status, codes) -> {
            MediaType mediaType = new MediaType()
                    .schema(new Schema<>().$ref(SCHEMA_REF));

            for (ErrorCode code : codes) {
                mediaType.addExamples(code.name(),
                        new Example()
                                .summary(code.getCode() + " · " + code.getMessage())
                                .value(buildBody(code)));
            }

            Content content = new Content().addMediaType("application/json", mediaType);
            String key = String.valueOf(status.value());

            ApiResponse existing = responses.get(key);
            if (existing != null) {
                // 컨트롤러가 @ApiResponses 로 직접 선언한 응답: description 은 그대로 두고 스키마·예시만 주입
                existing.setContent(content);
                if (existing.getDescription() == null || existing.getDescription().isBlank()) {
                    existing.setDescription(autoDescription(codes));
                }
            } else {
                // 선언 안 했으면 에러코드의 "코드 · 메시지" 로 description 자동 생성
                responses.addApiResponse(key, new ApiResponse()
                        .description(autoDescription(codes))
                        .content(content));
            }
        });

        return operation;
    }

    // 나열된 에러코드의 "코드 · 메시지" 로 description 자동 생성 (여러 개면 줄바꿈)
    private String autoDescription(List<ErrorCode> codes) {
        return codes.stream()
                .map(code -> code.getCode() + " · " + code.getMessage())
                .collect(Collectors.joining("<br>"));
    }

    // { "success": false, "data": null, "error": { "code": ..., "message": ... } }
    private Map<String, Object> buildBody(ErrorCode code) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("code", code.getCode());
        error.put("message", code.getMessage());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("data", null);
        body.put("error", error);
        return body;
    }
}
