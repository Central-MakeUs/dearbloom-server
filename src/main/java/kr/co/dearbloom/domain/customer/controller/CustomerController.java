package kr.co.dearbloom.domain.customer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.dearbloom.domain.customer.dto.request.CustomerCreateRequest;
import kr.co.dearbloom.domain.customer.dto.response.CustomerCreateResponse;
import kr.co.dearbloom.domain.customer.facade.CustomerFacade;
import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.global.dto.response.ApiResponse;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import kr.co.dearbloom.global.swagger.ApiErrorCodes;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Customer", description = "고객 API")
public class CustomerController {
    private final CustomerFacade customerFacade;

    @PostMapping
    @Operation(summary = "고객 계정 생성 (온보딩)",
            description = """
                    실명 / 학교를 받아 고객 프로필을 생성합니다.<br>
                    이름은 2-5자의 한글 또는 영문 실명, 학교는 한 곳만 선택합니다.<br>
                    회원가입 직후의 토큰에는 고객 프로필이 없으므로, 이 API 는
                    <b>activeRole 이 CUSTOMER 로 갱신된 새 accessToken</b> 을 함께 반환합니다 <br>
                    — 응답받는 즉시 기존 accessToken 을 교체해야 이후 고객 API 를 호출할 수 있습니다.<br>
                    refreshToken 은 재발급하지 않으며 그대로 사용합니다.<br>
                    이미 고객 프로필이 있으면 409 를 반환합니다.
                    """)
    @ApiErrorCodes({ErrorCode.EXPIRED_TOKEN, ErrorCode.UNIVERSITY_NOT_FOUND, ErrorCode.CUSTOMER_ALREADY_EXISTS})
    public ResponseEntity<ApiResponse<CustomerCreateResponse>> create(
            @AuthenticationPrincipal Member member,
            @RequestBody @Valid CustomerCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                customerFacade.create(member, request)
        ));
    }
}
