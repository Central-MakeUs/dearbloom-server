package kr.co.dearbloom.domain.customer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import kr.co.dearbloom.domain.artist.entity.artist.Region;
import kr.co.dearbloom.global.validation.validatator.ValidRealName;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 고객 프로필 수정 — 이름(필수)과 지역(선택)을 함께 보낸다. 지역을 null 로 보내면 미설정으로 비운다. */
@Getter
@NoArgsConstructor
public class CustomerProfileUpdateRequest {
    @NotBlank
    @ValidRealName
    @Schema(description = "고객 실명 (2-5자의 한글 또는 영문). 중복 허용.", example = "김디어")
    private String name;

    @Schema(description = "지역 (선택, 한 곳). 미선택 시 null. "
            + "가능한 값: SEOUL, GYEONGGI_NORTH, GYEONGGI_SOUTH, INCHEON, BUSAN, DAEGU, GWANGJU, "
            + "DAEJEON_SEJONG, ULSAN, GANGWON, CHUNGBUK, CHUNGNAM, JEONBUK, JEONNAM, GYEONGBUK, GYEONGNAM, JEJU",
            example = "SEOUL")
    private Region region;
}
