package kr.co.dearbloom.domain.customer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "학교를 선택해주세요")
    @Schema(description = "선택한 학교 ID (한 곳만 선택)", example = "1")
    private Long universityId;
}
