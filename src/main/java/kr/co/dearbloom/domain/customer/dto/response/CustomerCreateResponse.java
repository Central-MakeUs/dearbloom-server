package kr.co.dearbloom.domain.customer.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record CustomerCreateResponse(
        @Schema(description = "activeRole 이 CUSTOMER 로 갱신된 새 accessToken. 응답받는 즉시 기존 토큰을 교체해야 합니다.")
        String accessToken,

        @Schema(description = "생성된 고객 프로필")
        CustomerInfoResponse customer
) {
}
