package kr.co.dearbloom.domain.customer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.dearbloom.domain.customer.dto.request.CustomerNameUpdateRequest;
import kr.co.dearbloom.domain.customer.dto.response.CustomerDetailResponse;
import kr.co.dearbloom.domain.customer.dto.response.CustomerResponse;
import kr.co.dearbloom.domain.customer.entity.Customer;
import kr.co.dearbloom.domain.customer.facade.CustomerFacade;
import kr.co.dearbloom.domain.member.dto.RoleRevokeResponse;
import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.domain.member.facade.MemberFacade;
import kr.co.dearbloom.global.auth.resolver.CurrentCustomer;
import kr.co.dearbloom.global.dto.response.ApiResponse;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import kr.co.dearbloom.global.swagger.ApiErrorCodes;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
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
    private final MemberFacade memberFacade;

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

    @PatchMapping("/name")
    @Operation(summary = "고객 이름 수정",
            description = """
                    고객 이름을 수정합니다. 최초 등록은 고객 프로필 생성 API 에서 처리합니다.<br>
                    이름은 2-5자의 한글 또는 영문이며, <b>중복이 허용</b>됩니다.
                    """)
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN,
            ErrorCode.ROLE_ACCESS_DENIED, ErrorCode.CUSTOMER_NOT_FOUND})
    public ResponseEntity<ApiResponse<CustomerResponse>> updateName(
            @CurrentCustomer Customer customer,
            @RequestBody @Valid CustomerNameUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                customerFacade.updateName(customer, request)
        ));
    }

    @DeleteMapping
    @Operation(summary = "고객 역할 해지",
            description = """
                    현재 회원의 <b>고객 역할만</b> 해지합니다(계정 전체 탈퇴가 아님).<br>
                    작가 역할이 함께 있으면 고객 프로필은 익명화되고, <b>남은 작가 역할로 재발급된 accessToken</b> 을
                    응답으로 돌려줍니다 — <code>withdrawn=false</code>. 응답 즉시 기존 accessToken 을 교체하세요(refreshToken 은 유지).<br>
                    고객이 <b>유일한 역할</b>이면 계정 전체가 탈퇴 처리되어 <code>withdrawn=true</code> 로 내려갑니다 —
                    이때는 토큰을 삭제하고 로그인 화면으로 이동하세요.<br>
                    작가 모드로 로그인한 상태에서도 호출할 수 있습니다. 고객 역할이 없으면 403 을 반환합니다.
                    """)
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "고객 역할 해지 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "인증 필요 (토큰 없음/만료/유효하지 않음)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "고객 역할이 없음")
    })
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.ROLE_NOT_AVAILABLE})
    public ResponseEntity<ApiResponse<RoleRevokeResponse>> revokeCustomerRole(
            @AuthenticationPrincipal Member member
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                memberFacade.revokeCustomerRole(member)
        ));
    }
}
