package kr.co.dearbloom.domain.customer.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.dearbloom.domain.artist.entity.artist.Region;
import kr.co.dearbloom.domain.customer.entity.Customer;

/** 고객 정보 상세 조회 응답. */
public record CustomerDetailResponse(
        @Schema(description = "고객 ID", example = "1")
        Long customerId,

        @Schema(description = "고객 실명", example = "김디어")
        String name,

        @Schema(description = "학교 ID. 미설정 시 null.", example = "1")
        Long universityId,

        @Schema(description = "학교명. 미설정 시 null.", example = "서울대")
        String universityName,

        @Schema(description = "지역 유형. 미설정 시 null.", example = "SEOUL")
        Region region,

        @Schema(description = "지역 한글명. 미설정 시 null.", example = "서울")
        String regionLabel
) {
    public static CustomerDetailResponse from(Customer customer) {
        if (customer == null) {
            return null;
        }
        return new CustomerDetailResponse(
                customer.getCustomerId(),
                customer.getName(),
                customer.getUniversity() != null ? customer.getUniversity().getUniversityId() : null,
                customer.getUniversity() != null ? customer.getUniversity().getName() : null,
                customer.getRegion(),
                customer.getRegion() != null ? customer.getRegion().getLabel() : null
        );
    }
}
