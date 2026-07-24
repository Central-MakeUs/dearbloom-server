package kr.co.dearbloom.domain.customer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.dearbloom.domain.customer.dto.request.CustomerProfileUpdateRequest;
import kr.co.dearbloom.domain.customer.dto.response.CustomerDetailResponse;
import kr.co.dearbloom.domain.customer.dto.response.CustomerResponse;
import kr.co.dearbloom.domain.customer.entity.Customer;
import kr.co.dearbloom.domain.customer.facade.CustomerFacade;
import kr.co.dearbloom.global.auth.resolver.CurrentCustomer;
import kr.co.dearbloom.global.dto.response.ApiResponse;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import kr.co.dearbloom.global.swagger.ApiErrorCodes;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers/me")
@RequiredArgsConstructor
@Tag(name = "- Customer -", description = "고객 정보 관리 API")
public class CustomerController {
    private final CustomerFacade customerFacade;

    @GetMapping
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

    @PatchMapping
    @Operation(summary = "고객 프로필 수정",
            description = """
                    고객 프로필(이름 / 지역)을 수정합니다. 최초 등록은 고객 프로필 생성 API 에서 처리합니다.<br>
                    <b>이름</b>은 2-5자의 한글 또는 영문이며 중복이 허용됩니다(필수).<br>
                    <b>지역</b>은 선택이며, 보낸 값으로 교체됩니다 — null 을 보내면 지역이 비워집니다.
                    """)
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN,
            ErrorCode.ROLE_ACCESS_DENIED, ErrorCode.CUSTOMER_NOT_FOUND})
    public ResponseEntity<ApiResponse<CustomerResponse>> updateProfile(
            @CurrentCustomer Customer customer,
            @RequestBody @Valid CustomerProfileUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                customerFacade.updateProfile(customer, request)
        ));
    }
}
