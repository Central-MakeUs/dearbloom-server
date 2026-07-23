package kr.co.dearbloom.domain.customer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import kr.co.dearbloom.global.validation.validatator.ValidRealName;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CustomerNameUpdateRequest {
    @NotBlank
    @ValidRealName
    @Schema(description = "고객 실명 (2-5자의 한글 또는 영문). 중복 허용.", example = "김디어")
    private String name;
}
