package kr.co.dearbloom.global.dto.response.exception;

import kr.co.dearbloom.global.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    // 커스텀 예외 처리 핸들러
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException e) {
        log.info(e.getMessage(), e);
        ApiResponse<Void> response = ApiResponse.error(
                ErrorDetail.builder()
                        .code(e.getCode())
                        .message(e.getMessage())
                        .build()
        );
        return ResponseEntity.status(e.getHttpStatus()).body(response);
    }

    // Bean Validation 실패 처리 핸들러 (@Valid 검증 실패)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        log.info(e.getMessage(), e);
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("입력값이 올바르지 않습니다.");

        ApiResponse<Void> response = ApiResponse.error(
                ErrorDetail.builder()
                        .code("VALIDATION-400")
                        .message(message)
                        .build()
        );
        return ResponseEntity.status(400).body(response);
    }

    // 잘못된 요청 파라미터 처리 핸들러 (필수 파라미터 누락 / 타입 불일치)
    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception e) {
        log.info(e.getMessage(), e);
        ApiResponse<Void> response = ApiResponse.error(
                ErrorDetail.builder()
                        .code("REQUEST-400")
                        .message("요청 파라미터가 올바르지 않습니다.")
                        .build()
        );
        return ResponseEntity.status(400).body(response);
    }

    // 메서드 파라미터 제약 검증 실패 처리 핸들러 (@RequestParam @Positive 등)
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleHandlerMethodValidation(HandlerMethodValidationException e) {
        log.info(e.getMessage(), e);
        String message = e.getAllErrors().stream()
                .findFirst()
                .map(MessageSourceResolvable::getDefaultMessage)
                .orElse("요청 파라미터가 올바르지 않습니다.");
        ApiResponse<Void> response = ApiResponse.error(
                ErrorDetail.builder()
                        .code("REQUEST-400")
                        .message(message)
                        .build()
        );
        return ResponseEntity.status(400).body(response);
    }

    // 지원하지 않는 HTTP 메서드 처리 핸들러
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.info(e.getMessage(), e);
        ApiResponse<Void> response = ApiResponse.error(
                ErrorDetail.builder()
                        .code("METHOD-405")
                        .message("지원하지 않는 HTTP 메서드입니다.")
                        .build()
        );
        return ResponseEntity.status(405).body(response);
    }

    // 존재하지 않는 API 경로 요청 처리 핸들러
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.info(e.getMessage(), e);
        ApiResponse<Void> response = ApiResponse.error(
                ErrorDetail.builder()
                        .code("API-404")
                        .message("존재하지 않는 API 경로입니다.")
                        .build()
        );
        return ResponseEntity.status(404).body(response);
    }

    // 그외 모든 예외 처리 핸들러
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.info(e.getMessage(), e);
        ApiResponse<Void> response = ApiResponse.error(
                ErrorDetail.builder()
                        .code("SERVER-500")
                        .message("서버 내부 오류가 발생했습니다.")
                        .build()
        );
        return ResponseEntity.status(500).body(response);
    }
}
