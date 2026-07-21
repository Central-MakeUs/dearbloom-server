package kr.co.dearbloom.domain.customer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.dearbloom.domain.customer.dto.response.CustomerDetailResponse;
import kr.co.dearbloom.domain.customer.entity.Customer;
import kr.co.dearbloom.domain.customer.facade.CustomerFacade;
import kr.co.dearbloom.global.auth.resolver.CurrentCustomer;
import kr.co.dearbloom.global.dto.response.ApiResponse;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import kr.co.dearbloom.global.swagger.ApiErrorCodes;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Customer", description = "고객 API")
public class CustomerController {
    private final CustomerFacade customerFacade;

    @GetMapping("/me")
    @Operation(summary = "고객 정보 조회",
            description = """
                    현재 로그인한 고객의 정보(실명 / 학교)를 조회합니다.
                    """)
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN,
            ErrorCode.ROLE_ACCESS_DENIED, ErrorCode.CUSTOMER_NOT_FOUND})
    public ResponseEntity<ApiResponse<CustomerDetailResponse>> getMyInfo(
            @CurrentCustomer Customer customer
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                customerFacade.getMyInfo(customer)
        ));
    }
}
