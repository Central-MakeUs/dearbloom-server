package kr.co.dearbloom.domain.artwork.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.dearbloom.domain.artwork.dto.response.ArtworkDetailResponse;
import kr.co.dearbloom.domain.artwork.dto.response.ArtworkSummaryResponse;
import kr.co.dearbloom.domain.artwork.facade.ArtworkQueryFacade;
import kr.co.dearbloom.global.auth.resolver.CurrentViewer;
import kr.co.dearbloom.global.auth.resolver.ViewerContext;
import kr.co.dearbloom.global.dto.response.ApiResponse;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import kr.co.dearbloom.global.swagger.ApiErrorCodes;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/artworks")
@Tag(name = "Artwork - Viewer/Customer", description = "뷰어/고객 작품 조회 API")
public class ArtworkQueryController {
    private final ArtworkQueryFacade artworkQueryFacade;

    @GetMapping
    @Operation(summary = "작품 리스트 조회 (전체, 최신순)",
            description = """
                    전체 작품을 최신 등록순으로 조회합니다. 로그인하지 않아도 조회할 수 있습니다.<br>
                    각 항목은 작품 ID / 제목 / 가격 / 작가 닉네임 / 작가 활동지역 / 대표 이미지 / 저장 여부(isSaved)입니다.<br>
                    <b>isSaved</b> 는 고객 토큰으로 조회할 때만 채워지고, 비로그인은 null 입니다.
                    """)
    public ResponseEntity<ApiResponse<List<ArtworkSummaryResponse>>> getArtworkList(
            @CurrentViewer ViewerContext viewer
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                artworkQueryFacade.getArtworkList(viewer)
        ));
    }

    @GetMapping("/{artworkId}")
    @Operation(summary = "작품 상세 조회 (비로그인/고객)",
            description = """
                    작품 상세를 조회합니다. 로그인하지 않아도 조회할 수 있습니다.<br>
                    <b>고객 토큰</b>으로 조회하면 저장 여부(isSaved)가 채워지고, 비로그인은 null 입니다.<br>
                    작가 본인용 상세(저장 수/조회수 포함)는 <b>GET /api/artists/me/artworks/{artworkId}</b> 를 사용하세요.
                    """)
    @ApiErrorCodes({ErrorCode.ARTWORK_NOT_FOUND})
    public ResponseEntity<ApiResponse<ArtworkDetailResponse>> getDetail(
            @CurrentViewer ViewerContext viewer,
            @PathVariable Long artworkId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                artworkQueryFacade.getDetail(artworkId, viewer)
        ));
    }
}
