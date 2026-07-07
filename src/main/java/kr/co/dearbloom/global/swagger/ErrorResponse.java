package kr.co.dearbloom.global.swagger;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.dearbloom.global.dto.response.exception.ErrorDetail;

/**
 * Swagger 문서 전용 에러 응답 스키마.
 * 실제 응답은 {@code ApiResponse<Void>} 로 내려가지만, 제네릭 특성상 Swagger 에서 data 타입이 뭉개지므로
 * 에러 응답만 별도로 명세하기 위한 문서용 클래스다. (런타임 직렬화에는 사용하지 않음)
 */
@Schema(name = "ErrorResponse", description = "공통 에러 응답")
public class ErrorResponse {
    @Schema(description = "성공 여부", example = "false")
    public boolean success;

    @Schema(description = "에러 시 항상 null", nullable = true, example = "null")
    public Object data;

    public ErrorDetail error;
}
