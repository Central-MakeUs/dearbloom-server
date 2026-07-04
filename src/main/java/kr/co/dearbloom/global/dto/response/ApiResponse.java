package kr.co.dearbloom.global.dto.response;

import kr.co.dearbloom.global.dto.response.exception.ErrorDetail;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ApiResponse<T> {
    private boolean success;
    private T data;
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