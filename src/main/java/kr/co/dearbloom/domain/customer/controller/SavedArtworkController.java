package kr.co.dearbloom.domain.customer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.dearbloom.domain.artwork.dto.response.ArtworkSummaryResponse;
import kr.co.dearbloom.domain.customer.dto.request.SavedArtworkCreateRequest;
import kr.co.dearbloom.domain.customer.dto.request.SavedArtworkDeleteRequest;
import kr.co.dearbloom.domain.customer.entity.Customer;
import kr.co.dearbloom.domain.customer.facade.SavedArtworkFacade;
import kr.co.dearbloom.global.auth.resolver.CurrentCustomer;
import kr.co.dearbloom.global.dto.response.ApiResponse;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import kr.co.dearbloom.global.swagger.ApiErrorCodes;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customers/me/saved-artworks")
@Tag(name = "Saved Artwork", description = "고객 작품 저장 API")
public class SavedArtworkController {
    private final SavedArtworkFacade savedArtworkFacade;

    @PostMapping
    @Operation(summary = "작품 저장",
            description = """
                    작품 하나를 저장합니다. artworkId 로 저장합니다.<br>
                    이미 저장한 작품이면 409 를 반환합니다.
                    """)
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.ROLE_ACCESS_DENIED,
            ErrorCode.CUSTOMER_NOT_FOUND, ErrorCode.ARTWORK_NOT_FOUND, ErrorCode.ARTWORK_ALREADY_SAVED})
    public ResponseEntity<ApiResponse<Void>> save(
            @CurrentCustomer Customer customer,
            @RequestBody @Valid SavedArtworkCreateRequest request
    ) {
        savedArtworkFacade.save(customer, request.getArtworkId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success());
    }

    @GetMapping
    @Operation(summary = "내 저장 목록 조회",
            description = """
                    내가 저장한 작품 목록을 저장 최신순으로 조회합니다.<br>
                    각 항목은 작품 ID / 제목 / 가격 / 작가 닉네임 / 대표 이미지입니다.
                    """)
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.ROLE_ACCESS_DENIED,
            ErrorCode.CUSTOMER_NOT_FOUND})
    public ResponseEntity<ApiResponse<List<ArtworkSummaryResponse>>> getSavedList(
            @CurrentCustomer Customer customer
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                savedArtworkFacade.getSavedList(customer)
        ));
    }

    @DeleteMapping("/{artworkId}")
    @Operation(summary = "작품 저장 취소 (단일)",
            description = """
                    저장한 작품 하나를 저장 취소합니다. 저장돼 있지 않아도 정상 처리됩니다(멱등).
                    """)
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.ROLE_ACCESS_DENIED,
            ErrorCode.CUSTOMER_NOT_FOUND})
    public ResponseEntity<ApiResponse<Void>> delete(
            @CurrentCustomer Customer customer,
            @PathVariable Long artworkId
    ) {
        savedArtworkFacade.delete(customer, artworkId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @DeleteMapping
    @Operation(summary = "작품 저장 취소 (다중)",
            description = """
                    저장한 작품 여러 개를 한 번에 저장 취소합니다. artworkIdList 로 전달합니다.<br>
                    저장돼 있지 않은 ID 가 섞여 있어도 정상 처리됩니다.
                    """)
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.ROLE_ACCESS_DENIED,
            ErrorCode.CUSTOMER_NOT_FOUND})
    public ResponseEntity<ApiResponse<Void>> deleteAll(
            @CurrentCustomer Customer customer,
            @RequestBody @Valid SavedArtworkDeleteRequest request
    ) {
        savedArtworkFacade.deleteAll(customer, request);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
