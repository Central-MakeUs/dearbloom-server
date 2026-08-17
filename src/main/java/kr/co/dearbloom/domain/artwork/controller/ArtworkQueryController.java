package kr.co.dearbloom.domain.artwork.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.dearbloom.domain.artwork.dto.request.ArtworkQueryRequest;
import kr.co.dearbloom.domain.artwork.dto.response.ArtworkDetailResponse;
import kr.co.dearbloom.domain.artwork.dto.response.ArtworkPageResponse;
import kr.co.dearbloom.domain.artwork.facade.ArtworkQueryFacade;
import kr.co.dearbloom.global.auth.resolver.CurrentViewer;
import kr.co.dearbloom.global.auth.resolver.ViewerContext;
import kr.co.dearbloom.global.dto.response.ApiResponse;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import kr.co.dearbloom.global.swagger.ApiErrorCodes;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/artworks")
@Tag(name = "Artwork - Viewer/Customer", description = "뷰어/고객 작품 조회 API")
public class ArtworkQueryController {
    private final ArtworkQueryFacade artworkQueryFacade;

    @GetMapping
    @Operation(summary = "작품 탐색 페이지 조회 (필터·정렬·무한스크롤)",
            description = """
                    작품을 필터·정렬해서 조회합니다. 로그인하지 않아도 조회할 수 있습니다.<br>
                    각 항목은 작품 ID / 제목 / 가격 / 촬영 가능 인원 / 작가 닉네임 / 작가 활동지역 / 대표 이미지 / 저장 여부(isSaved)입니다.<br>
                    <b>isSaved</b> 는 고객 토큰으로 조회할 때만 채워지고, 비로그인은 null 입니다.<br>
                    <br>
                    <b>파라미터를 하나도 보내지 않으면</b>(= 작품 탐색 첫 진입 화면) 필터 없이 전체 작품을
                    <b>최신순(LATEST)으로 첫 페이지(16개)</b>를 돌려줍니다. 그 상태에서 필터·정렬 UI 를 조작할 때만
                    해당 파라미터를 채워 보내면 됩니다.<br>
                    <br>
                    <b>필터</b> — 모두 선택 사항이고, 보내지 않은 조건은 적용하지 않습니다.<br>
                    • <b>날짜</b>(startDate + endDate): 그 기간 중 <b>하루라도</b> 촬영 가능한 작가의 작품을 보여줍니다.
                    작가의 요일 반복 일정(기본 촬영 가능) 기준이며, 특정 날짜의 개인 예약 불가·예약 확정까지는 반영하지 않습니다.
                    하루만 고르려면 startDate 와 endDate 를 같은 날로 보냅니다. 최대 30일까지 허용합니다.<br>
                    • <b>지역</b>(region): 작가 활동 지역에 포함된 작품만.<br>
                    • <b>인원</b>(headCount): 작품의 minHeadCount ~ maxHeadCount 범위에 들어가는 작품만. 6 은 "6인 이상"입니다.<br>
                    <br>
                    <b>정렬</b>(sort) — 보내지 않으면 <b>LATEST</b> 입니다.<br>
                    • <b>LATEST</b>: 기본순. 등록 최신순입니다. 화면의 "추천순"을 대체<br>
                    • <b>PRICE_LOW</b>: 낮은 가격순.<br>
                    • <b>PRICE_HIGH</b>: 높은 가격순.<br>
                    가격 정렬은 카드에 노출되는 <b>가격</b>(lowestPrice — 패키지 중 최저가) 기준입니다.
                    가격이 같은 작품끼리는 항상 같은 순서로 나옵니다 — 순서가 흔들리면 스크롤 중에 작품이 누락됩니다.<br>
                    <br>
                    <b>무한스크롤</b> — 한 페이지는 <b>16개 고정</b>입니다(요청으로 조절하지 않습니다).
                    첫 페이지는 cursor 없이 요청하고, 이후에는 응답의 <b>nextCursor</b> 를 그대로 실어 보냅니다.
                    <b>hasNext</b> 가 false 면 마지막 페이지입니다. 스크롤 중에는 필터·정렬을 바꾸지 말고,
                    바꿀 때는 cursor 를 비워 처음부터 다시 받습니다.<br>
                    <b>totalCount</b> 는 현재 필터를 만족하는 전체 작품 수로, 매 페이지 응답에 함께 내려갑니다.
                    """)
    @ApiErrorCodes({ErrorCode.PARAMETER_BAD_REQUEST, ErrorCode.INVALID_CURSOR})
    public ResponseEntity<ApiResponse<ArtworkPageResponse>> getArtworkList(
            @CurrentViewer ViewerContext viewer,
            @ParameterObject @Valid @ModelAttribute ArtworkQueryRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                artworkQueryFacade.getArtworkPage(request, viewer)
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
