package kr.co.dearbloom.domain.customer.dto.response;

import kr.co.dearbloom.domain.artist.entity.artist.Region;
import kr.co.dearbloom.domain.customer.entity.Customer;

public record CustomerResponse(
        Long customerId,
        String name,
        Long universityId,
        String universityName,
        Region region,
        String regionLabel
) {
    public static CustomerResponse from(Customer customer) {
        if (customer == null) {
            return null;
        }
        return new CustomerResponse(
                customer.getCustomerId(),
                customer.getName(),
                customer.getUniversity() != null ? customer.getUniversity().getUniversityId() : null,
                customer.getUniversity() != null ? customer.getUniversity().getName() : null,
                customer.getRegion(),
                customer.getRegion() != null ? customer.getRegion().getLabel() : null
        );
    }
}
