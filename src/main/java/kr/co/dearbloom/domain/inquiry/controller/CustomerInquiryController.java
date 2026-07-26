package kr.co.dearbloom.domain.inquiry.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.dearbloom.domain.customer.entity.Customer;
import kr.co.dearbloom.domain.inquiry.dto.request.InquiryCreateRequest;
import kr.co.dearbloom.domain.inquiry.dto.response.customer.CustomerInquiryDetailResponse;
import kr.co.dearbloom.domain.inquiry.dto.response.customer.CustomerInquiryListItemResponse;
import kr.co.dearbloom.domain.inquiry.dto.response.InquiryCreateResponse;
import kr.co.dearbloom.domain.inquiry.dto.response.customer.InquiryPreparationResponse;
import kr.co.dearbloom.domain.inquiry.dto.response.InquiryStatusResponse;
import kr.co.dearbloom.domain.inquiry.facade.CustomerInquiryFacade;
import kr.co.dearbloom.global.auth.resolver.CurrentCustomer;
import kr.co.dearbloom.global.dto.response.ApiResponse;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import kr.co.dearbloom.global.swagger.ApiErrorCodes;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inquiries")
@RequiredArgsConstructor
@Tag(name = "Inquiry - Customer", description = "고객 문의 관리 API")
public class CustomerInquiryController {
    private final CustomerInquiryFacade customerInquiryFacade;

    @GetMapping("/preparation")
    @Operation(summary = "스마트 문의 준비 정보 조회",
            description = """
                    작품 상세에서 고른 패키지로 문의 화면에 진입할 때, 한 번에 필요한 정보를 조회합니다.<br>
                    <b>artworkPackageId</b> 하나만 보내면 됩니다(내 정보는 인증 토큰에서).<br><br>
                    <b>응답 포함</b>
                    <br>• 확인 화면 헤더: 작품명 / 작가 닉네임 / 대표 이미지
                    <br>• 패키지 메타: 패키지명 / 가격 / 촬영 소요시간 / <b>연속 선택 셀 수(requiredSlotCount)</b> / 슬롯 단위(30분)
                    <br>• 촬영 가능 인원 범위: minHeadCount / maxHeadCount (작품 기준)
                    <br>• 작가 <b>3개월 가용 캘린더</b>(날짜별 예약 가능 시간). 시간 탭 시 requiredSlotCount 만큼 연속 선택은 클라에서 처리.
                    """)
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN,
            ErrorCode.ROLE_ACCESS_DENIED, ErrorCode.CUSTOMER_NOT_FOUND, ErrorCode.ARTWORK_PACKAGE_NOT_FOUND})
    public ResponseEntity<ApiResponse<InquiryPreparationResponse>> getPreparation(
            @CurrentCustomer Customer customer,
            @RequestParam Long artworkPackageId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                customerInquiryFacade.getPreparation(artworkPackageId)
        ));
    }

    @PostMapping
    @Operation(summary = "스마트 문의 전송",
            description = """
                    선택한 패키지·날짜·시간·학교·인원·요청사항으로 문의를 전송합니다.<br><br>
                    <b>학교</b>: 대학 목록에서 골랐으면 <code>universityId</code>, 목록에 없어 직접 입력했으면 <code>schoolName</code> 을 보냅니다(둘 중 하나 필수).<br>
                    <b>시간</b>: startTime 은 09:00~21:00, 30분 단위. 서버가 <b>그 시각부터 패키지 소요시간만큼 연속 예약 가능한지</b> 재검증합니다.<br>
                    <b>인원</b>: 작품의 min~max 범위를 벗어나면 400.<br>
                    작가/작품/패키지명·가격은 문의 시점 스냅샷으로 저장되어 이후 수정/삭제돼도 보존됩니다.<br>
                    문의는 슬롯을 잠그지 않습니다. 예약 완료시 슬롯을 잠급니다.
                    """)
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.ROLE_ACCESS_DENIED,
            ErrorCode.CUSTOMER_NOT_FOUND, ErrorCode.ARTWORK_PACKAGE_NOT_FOUND, ErrorCode.UNIVERSITY_NOT_FOUND,
            ErrorCode.INQUIRY_SCHOOL_REQUIRED, ErrorCode.INQUIRY_INVALID_HEAD_COUNT, ErrorCode.INQUIRY_SLOT_NOT_AVAILABLE})
    public ResponseEntity<ApiResponse<InquiryCreateResponse>> createInquiry(
            @CurrentCustomer Customer customer,
            @RequestBody @Valid InquiryCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                customerInquiryFacade.createInquiry(customer, request)
        ));
    }

    @GetMapping
    @Operation(summary = "내 문의 리스트 조회",
            description = "내가 보낸 문의를 <b>최근 수정순</b>으로 조회합니다. 필터 없이 전체를 반환합니다.")
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN,
            ErrorCode.ROLE_ACCESS_DENIED, ErrorCode.CUSTOMER_NOT_FOUND})
    public ResponseEntity<ApiResponse<List<CustomerInquiryListItemResponse>>> getMyInquiries(
            @CurrentCustomer Customer customer
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                customerInquiryFacade.getMyInquiries(customer)
        ));
    }

    @GetMapping("/{inquiryId}")
    @Operation(summary = "내 문의 상세 조회",
            description = "본인이 보낸 문의의 상세를 조회합니다.")
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.ROLE_ACCESS_DENIED,
            ErrorCode.CUSTOMER_NOT_FOUND, ErrorCode.INQUIRY_NOT_FOUND, ErrorCode.INQUIRY_ACCESS_DENIED})
    public ResponseEntity<ApiResponse<CustomerInquiryDetailResponse>> getMyInquiryDetail(
            @CurrentCustomer Customer customer,
            @PathVariable Long inquiryId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                customerInquiryFacade.getMyInquiryDetail(customer, inquiryId)
        ));
    }

    @PatchMapping("/{inquiryId}/cancel")
    @Operation(summary = "문의 취소",
            description = "본인이 보낸 문의를 취소합니다. <b>문의 진행중</b> 상태에서만 가능하며, 취소하면 문의 취소 상태가 됩니다. "
                    + "이미 예약 완료된 건은 취소할 수 없습니다(작가의 예약 취소만 가능).")
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.ROLE_ACCESS_DENIED,
            ErrorCode.CUSTOMER_NOT_FOUND, ErrorCode.INQUIRY_NOT_FOUND, ErrorCode.INQUIRY_ACCESS_DENIED,
            ErrorCode.INQUIRY_INVALID_STATUS})
    public ResponseEntity<ApiResponse<InquiryStatusResponse>> cancelInquiry(
            @CurrentCustomer Customer customer,
            @PathVariable Long inquiryId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                customerInquiryFacade.cancelInquiry(customer, inquiryId)
        ));
    }
}
