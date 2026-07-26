package kr.co.dearbloom.domain.inquiry.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.dearbloom.domain.artist.entity.artist.Artist;
import kr.co.dearbloom.domain.inquiry.dto.response.InquiryHistoryResponse;
import kr.co.dearbloom.domain.inquiry.dto.response.artist.ArtistInquiryDetailResponse;
import kr.co.dearbloom.domain.inquiry.dto.response.artist.ArtistInquiryListItemResponse;
import kr.co.dearbloom.domain.inquiry.dto.response.InquiryStatusResponse;
import kr.co.dearbloom.domain.inquiry.facade.ArtistInquiryFacade;
import kr.co.dearbloom.global.auth.resolver.CurrentArtist;
import kr.co.dearbloom.global.dto.response.ApiResponse;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import kr.co.dearbloom.global.swagger.ApiErrorCodes;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 작가의 문의 관리(상태 전이). 본인 작품에 들어온 문의에 대해서만 동작한다.
 * 문의 취소는 고객·작가 둘 다 가능하지만, 예약 완료·예약 취소는 작가만 가능하다.
 */
@RestController
@RequestMapping("/api/artists/me/inquiries")
@RequiredArgsConstructor
@Tag(name = "Inquiry - Artist", description = "작가 문의 관리 API")
public class ArtistInquiryController {
    private final ArtistInquiryFacade artistInquiryFacade;

    @GetMapping
    @Operation(summary = "받은 문의 리스트 조회",
            description = "내 작품에 들어온 문의를 <b>촬영 예정일 오름차순</b>(같은 날은 시작시각 순)으로 조회합니다. 필터 없이 전체를 반환합니다.")
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN,
            ErrorCode.ROLE_ACCESS_DENIED, ErrorCode.ARTIST_NOT_FOUND})
    public ResponseEntity<ApiResponse<List<ArtistInquiryListItemResponse>>> getInquiries(
            @CurrentArtist Artist artist
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                artistInquiryFacade.getInquiries(artist)
        ));
    }

    @GetMapping("/{inquiryId}")
    @Operation(summary = "받은 문의 상세 조회",
            description = "내 작품에 들어온 문의의 상세를 조회합니다.")
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.ROLE_ACCESS_DENIED,
            ErrorCode.ARTIST_NOT_FOUND, ErrorCode.INQUIRY_NOT_FOUND, ErrorCode.INQUIRY_ACCESS_DENIED})
    public ResponseEntity<ApiResponse<ArtistInquiryDetailResponse>> getInquiryDetail(
            @CurrentArtist Artist artist,
            @PathVariable Long inquiryId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                artistInquiryFacade.getInquiryDetail(artist, inquiryId)
        ));
    }

    @GetMapping("/{inquiryId}/history")
    @Operation(summary = "받은 문의 상태 변경 이력 조회",
            description = "문의의 상태 변경 타임라인을 <b>오래된 순</b>으로 조회합니다. 각 항목은 변경 전/후 상태, 변경 주체(고객/작가), 변경 시각입니다.")
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.ROLE_ACCESS_DENIED,
            ErrorCode.ARTIST_NOT_FOUND, ErrorCode.INQUIRY_NOT_FOUND, ErrorCode.INQUIRY_ACCESS_DENIED})
    public ResponseEntity<ApiResponse<List<InquiryHistoryResponse>>> getInquiryHistory(
            @CurrentArtist Artist artist,
            @PathVariable Long inquiryId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                artistInquiryFacade.getInquiryHistory(artist, inquiryId)
        ));
    }

    @PatchMapping("/{inquiryId}/cancel")
    @Operation(summary = "문의 취소",
            description = "본인 작품에 들어온 문의를 취소합니다. <b>문의 진행중</b> 상태에서만 가능합니다.")
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.ROLE_ACCESS_DENIED,
            ErrorCode.ARTIST_NOT_FOUND, ErrorCode.INQUIRY_NOT_FOUND, ErrorCode.INQUIRY_ACCESS_DENIED,
            ErrorCode.INQUIRY_INVALID_STATUS})
    public ResponseEntity<ApiResponse<InquiryStatusResponse>> cancelInquiry(
            @CurrentArtist Artist artist,
            @PathVariable Long inquiryId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                artistInquiryFacade.cancelInquiry(artist, inquiryId)
        ));
    }

    @PatchMapping("/{inquiryId}/reserve")
    @Operation(summary = "예약 완료",
            description = "문의를 예약 완료로 확정합니다. <b>문의 진행중</b> 상태에서만 가능하며, 확정 시 "
                    + "해당 시간대 슬롯이 잠깁니다. 다른 예약과 겹치면 409 를 반환합니다. 결제는 없으며 예약 완료가 마지막 단계입니다.")
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.ROLE_ACCESS_DENIED,
            ErrorCode.ARTIST_NOT_FOUND, ErrorCode.INQUIRY_NOT_FOUND, ErrorCode.INQUIRY_ACCESS_DENIED,
            ErrorCode.INQUIRY_INVALID_STATUS, ErrorCode.INQUIRY_SLOT_NOT_AVAILABLE})
    public ResponseEntity<ApiResponse<InquiryStatusResponse>> completeReservation(
            @CurrentArtist Artist artist,
            @PathVariable Long inquiryId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                artistInquiryFacade.completeReservation(artist, inquiryId)
        ));
    }

    @PatchMapping("/{inquiryId}/reserve-cancel")
    @Operation(summary = "예약 취소",
            description = "예약 완료된 건을 취소합니다. <b>예약 완료</b> 상태에서만 가능하며, 취소 시 해당 시간대 슬롯이 다시 열립니다.")
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.ROLE_ACCESS_DENIED,
            ErrorCode.ARTIST_NOT_FOUND, ErrorCode.INQUIRY_NOT_FOUND, ErrorCode.INQUIRY_ACCESS_DENIED,
            ErrorCode.INQUIRY_INVALID_STATUS})
    public ResponseEntity<ApiResponse<InquiryStatusResponse>> cancelReservation(
            @CurrentArtist Artist artist,
            @PathVariable Long inquiryId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                artistInquiryFacade.cancelReservation(artist, inquiryId)
        ));
    }
}
