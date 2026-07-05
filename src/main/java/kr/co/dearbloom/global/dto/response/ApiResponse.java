package kr.co.dearbloom.global.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.dearbloom.global.dto.response.exception.ErrorDetail;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL) // null 필드는 응답에서 제외 (성공 시 error, 실패 시 data)
public class ApiResponse<T> {
    private boolean success;
    private T data;

    // 성공 응답 문서에는 노출하지 않음. 에러 응답은 별도 ErrorResponse 스키마로 문서화됨.
    @Schema(hidden = true)
    private ErrorDetail error;

    // ──────── 성공 응답 생성 메서드 ────────
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .error(null)
                .build();
    }

    public static ApiResponse<Void> success() {
        return ApiResponse.<Void>builder()
                .success(true)
                .data(null)
                .error(null)
                .build();
    }

    // ──────── 실패 응답 생성 메서드 ────────
    public static <T> ApiResponse<T> error(ErrorDetail error) {
        return ApiResponse.<T>builder()
                .success(false)
                .data(null)
                .error(error)
                .build();
    }
}