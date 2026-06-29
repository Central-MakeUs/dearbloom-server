package kr.co.dearbloom.global.dto.response.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorDetail {
    private String code;        // 에러 코드
    private String message;      // 상세 설명
//        private String field;       // 에러 발생 필드
}
