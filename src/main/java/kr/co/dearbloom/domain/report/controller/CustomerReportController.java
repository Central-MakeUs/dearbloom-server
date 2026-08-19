package kr.co.dearbloom.domain.report.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.dearbloom.domain.customer.entity.Customer;
import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.domain.report.dto.request.ArtworkReportCreateRequest;
import kr.co.dearbloom.domain.report.dto.response.ArtworkReportedResponse;
import kr.co.dearbloom.domain.report.facade.ReportFacade;
import kr.co.dearbloom.global.auth.resolver.CurrentCustomer;
import kr.co.dearbloom.global.dto.response.ApiResponse;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import kr.co.dearbloom.global.swagger.ApiErrorCodes;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customers/me")
@Tag(name = "2-6 [Customer] Report", description = "고객 신고 API")
public class CustomerReportController {
    private final ReportFacade reportFacade;

    @PostMapping("/artwork-reports")
    @Operation(summary = "작품 신고",
            description = """
                    작품 하나를 신고합니다. artworkId 와 신고 사유(content) 를 보냅니다.<br>
                    신고 사유는 자유 텍스트이며 최대 1000자입니다(필수).<br>
                    같은 작품을 이미 신고했다면 409 를 반환합니다 — 신고 취소는 없습니다.
                    """)
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.ROLE_ACCESS_DENIED,
            ErrorCode.CUSTOMER_NOT_FOUND, ErrorCode.ARTWORK_NOT_FOUND, ErrorCode.ALREADY_REPORTED})
    public ResponseEntity<ApiResponse<Void>> report(
            @AuthenticationPrincipal Member member,
            @RequestBody @Valid ArtworkReportCreateRequest request
    ) {
        reportFacade.reportArtwork(member, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success());
    }

    @GetMapping("/artwork-reports/{artworkId}")
    @Operation(summary = "작품 신고 여부 조회",
            description = """
                    내가 해당 작품을 신고했는지 여부를 반환합니다.<br>
                    작품 상세에서 '신고하기' 버튼의 활성/비활성 판단에 사용합니다.
                    """)
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.ROLE_ACCESS_DENIED,
            ErrorCode.CUSTOMER_NOT_FOUND, ErrorCode.ARTWORK_NOT_FOUND})
    public ResponseEntity<ApiResponse<ArtworkReportedResponse>> isReported(
            @AuthenticationPrincipal Member member,
            @PathVariable Long artworkId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                reportFacade.isArtworkReported(member, artworkId)
        ));
    }
}
