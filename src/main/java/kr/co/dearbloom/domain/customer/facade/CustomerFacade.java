package kr.co.dearbloom.domain.customer.facade;

import kr.co.dearbloom.domain.customer.dto.request.CustomerProfileUpdateRequest;
import kr.co.dearbloom.domain.customer.dto.response.CustomerDetailResponse;
import kr.co.dearbloom.domain.customer.dto.response.CustomerResponse;
import kr.co.dearbloom.domain.customer.entity.Customer;
import kr.co.dearbloom.domain.customer.service.CustomerCommandService;
import kr.co.dearbloom.domain.customer.service.CustomerQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CustomerFacade {
    private final CustomerQueryService customerQueryService;
    private final CustomerCommandService customerCommandService;

    // university 를 트랜잭션 안에서 매핑까지 끝내야 하므로 재조회 후 매핑한다(open-in-view: false).
    @Transactional(readOnly = true)
    public CustomerDetailResponse getMyInfo(Customer customer) {
        return CustomerDetailResponse.from(
                customerQueryService.getById(customer.getCustomerId())
        );
    }

    @Transactional
    public CustomerResponse updateProfile(Customer customer, CustomerProfileUpdateRequest request) {
        return CustomerResponse.from(
                customerCommandService.updateProfile(customer.getCustomerId(), request.getName(), request.getRegion())
        );
    }
}
