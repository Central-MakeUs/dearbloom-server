package kr.co.dearbloom.domain.customer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import kr.co.dearbloom.global.validation.validatator.ValidRealName;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CustomerCreateRequest {
    @NotBlank
    @ValidRealName
    @Schema(description = "고객 실명 (2-5자의 한글 또는 영문)", example = "김디어")
    private String name;

    @Schema(description = "선택한 학교 ID (한 곳만 선택). 대학생이 아니면 null 가능.", example = "1")
    private Long universityId;
}
