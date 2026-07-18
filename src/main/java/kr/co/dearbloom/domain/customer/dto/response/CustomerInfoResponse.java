package kr.co.dearbloom.domain.customer.dto.response;

import kr.co.dearbloom.domain.customer.entity.Customer;

public record CustomerInfoResponse(
        Long customerId,
        String name,
        Long universityId,
        String universityName
) {
    public static CustomerInfoResponse from(Customer customer) {
        if (customer == null) {
            return null;
        }
        return new CustomerInfoResponse(
                customer.getCustomerId(),
                customer.getName(),
                customer.getUniversity() != null ? customer.getUniversity().getUniversityId() : null,
                customer.getUniversity() != null ? customer.getUniversity().getName() : null
        );
    }
}
