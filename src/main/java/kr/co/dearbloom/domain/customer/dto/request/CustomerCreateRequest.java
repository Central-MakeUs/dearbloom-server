package kr.co.dearbloom.domain.customer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import kr.co.dearbloom.domain.artist.entity.artist.Region;
import kr.co.dearbloom.global.validation.validatator.ValidRealName;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CustomerCreateRequest {
    @NotBlank
    @ValidRealName
    @Schema(description = "고객 실명 (2-5자의 한글 또는 영문, 공백·숫자 불가)", example = "김디어")
    private String name;

    @Schema(description = "선택한 학교 ID (한 곳만 선택). 대학생이 아니면 null 가능.", example = "1")
    private Long universityId;

    @Schema(description = "지역 (선택, 한 곳). 미선택 시 null. "
            + "가능한 값: SEOUL, GYEONGGI_NORTH, GYEONGGI_SOUTH, INCHEON, BUSAN, DAEGU, GWANGJU, "
            + "DAEJEON_SEJONG, ULSAN, GANGWON, CHUNGBUK, CHUNGNAM, JEONBUK, JEONNAM, GYEONGBUK, GYEONGNAM, JEJU",
            example = "SEOUL")
    private Region region;
}
