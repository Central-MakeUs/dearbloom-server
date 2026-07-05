package kr.co.dearbloom.global.dto.response.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorDetail {
    @Schema(description = "에러 코드", example = "REQUEST-400")
    private String code;        // 에러 코드

    @Schema(description = "상세 설명", example = "요청 파라미터가 올바르지 않습니다.")
    private String message;      // 상세 설명
//        private String field;       // 에러 발생 필드
}
