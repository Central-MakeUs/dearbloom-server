package kr.co.dearbloom.domain.artwork.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.dearbloom.domain.artist.entity.artist.Artist;
import kr.co.dearbloom.domain.artwork.dto.request.ArtworkCreateRequest;
import kr.co.dearbloom.domain.artwork.dto.request.ArtworkPhotoUpdateRequest;
import kr.co.dearbloom.domain.artwork.dto.request.ArtworkTitleUpdateRequest;
import kr.co.dearbloom.domain.artwork.dto.response.ArtworkDetailResponse;
import kr.co.dearbloom.domain.artwork.dto.response.ArtworkResponse;
import kr.co.dearbloom.domain.artwork.dto.response.ArtworkSummaryResponse;
import kr.co.dearbloom.domain.artwork.facade.ArtworkCommandFacade;
import kr.co.dearbloom.domain.artwork.facade.ArtworkQueryFacade;
import kr.co.dearbloom.global.auth.resolver.CurrentArtist;
import kr.co.dearbloom.global.auth.resolver.CurrentViewer;
import kr.co.dearbloom.global.auth.resolver.ViewerContext;
import kr.co.dearbloom.global.dto.response.ApiResponse;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import kr.co.dearbloom.global.swagger.ApiErrorCodes;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/artworks")
@Tag(name = "Artwork", description = "작품 API")
public class ArtworkController {
    private final ArtworkCommandFacade artworkCommandFacade;
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

    @PostMapping
    @Operation(summary = "작품 등록",
            description = """
                    작품 제목 / 촬영 가능 인원 / 패키지들 / 사진들을 받아 작품을 등록합니다.<br>
                    <b>패키지는 1개 이상 필수</b>이며, 리스트 화면에는 패키지 중 최저가가 노출됩니다.
                    (가격은 작품이 아니라 각 패키지에 있습니다.)<br>
                    사진은 1장 이상 필수이며, 각 사진마다 학교 ID 를 1개씩 라벨링할 수 있습니다
                    (학교는 선택이라 null 가능).<br>
                    사진은 등록한 순서대로 정렬됩니다(임시). 각 fileUrl 은 File API 의 presigned URL 로
                    S3 업로드를 완료한 뒤의 CDN URL 이어야 합니다.<br><br>
                    <b>촬영 가능 인원(minHeadCount / maxHeadCount)</b><br>
                    인원은 1~6명 범위이며, 화면 표기에 따라 아래처럼 조합합니다.<br>
                    - <b>"N인"</b> (예: 2인): minHeadCount=2, maxHeadCount=2 (동일한 값)<br>
                    - <b>"N~M인"</b> (예: 2~4인): minHeadCount=2, maxHeadCount=4<br>
                    - <b>"N인 이상"</b> (예: 3인 이상): minHeadCount=3, <b>maxHeadCount 는 보내지 않음(null)</b><br>
                    maxHeadCount 를 보내는 경우 minHeadCount 보다 작을 수 없습니다.
                    """)
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.ROLE_ACCESS_DENIED,
            ErrorCode.ARTIST_NOT_FOUND, ErrorCode.INVALID_FILE_URL, ErrorCode.UNIVERSITY_NOT_FOUND})
    public ResponseEntity<ApiResponse<ArtworkResponse>> create(
            @CurrentArtist Artist artist,
            @RequestBody @Valid ArtworkCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                artworkCommandFacade.create(artist, request)
        ));
    }

    @PatchMapping("/{artworkId}/title")
    @Operation(summary = "작품 제목 수정",
            description = """
                    작품의 제목을 수정합니다. <b>사진·패키지는 이 API 로 변경되지 않습니다</b>
                    (사진 변경은 사진 교체 API 사용).<br>
                    title 을 보내지 않거나 null 이면 변경하지 않습니다. 본인 작품만 수정할 수 있습니다.<br>
                    응답에는 변경된 작품 정보와 함께 기존 패키지·사진 목록도 포함됩니다.
                    """)
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.ROLE_ACCESS_DENIED,
            ErrorCode.ARTIST_NOT_FOUND, ErrorCode.ARTWORK_NOT_FOUND, ErrorCode.ARTWORK_ACCESS_DENIED})
    public ResponseEntity<ApiResponse<ArtworkResponse>> updateTitle(
            @CurrentArtist Artist artist,
            @PathVariable Long artworkId,
            @RequestBody @Valid ArtworkTitleUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                artworkCommandFacade.updateTitle(artist, artworkId, request)
        ));
    }

    @PutMapping("/{artworkId}/photos")
    @Operation(summary = "작품 사진 교체",
            description = """
                    작품에 최종적으로 보이길 원하는 <b>사진 전체 목록</b>을 보내주세요(부분 수정 아님).
                    보낸 목록이 그대로 작품의 사진이 됩니다.<br>
                    - <b>유지할 기존 사진</b>: 그 사진의 기존 fileUrl 을 그대로 다시 포함해 보내면 됩니다(다시 업로드할 필요 없음).<br>
                    - <b>새로 추가할 사진</b>: presigned 업로드를 마친 새 fileUrl 을 포함.<br>
                    - <b>목록에서 뺀 사진</b>: 작품에서 사라집니다.<br>
                    보낸 순서대로 화면에 정렬되며, 사진은 1장 이상 필수입니다.
                    각 사진마다 학교(선택, null 가능)를 라벨링합니다. 본인 작품만 수정할 수 있습니다.
                    """)
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.ROLE_ACCESS_DENIED,
            ErrorCode.ARTIST_NOT_FOUND, ErrorCode.ARTWORK_NOT_FOUND, ErrorCode.ARTWORK_ACCESS_DENIED,
            ErrorCode.INVALID_FILE_URL, ErrorCode.UNIVERSITY_NOT_FOUND})
    public ResponseEntity<ApiResponse<ArtworkResponse>> replacePhotos(
            @CurrentArtist Artist artist,
            @PathVariable Long artworkId,
            @RequestBody @Valid ArtworkPhotoUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                artworkCommandFacade.replacePhotos(artist, artworkId, request)
        ));
    }

    @DeleteMapping("/{artworkId}")
    @Operation(summary = "작품 삭제",
            description = """
                    작품을 삭제합니다. 작품에 등록된 사진도 함께 삭제됩니다.<br>
                    본인 작품만 삭제할 수 있습니다.
                    """)
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.ROLE_ACCESS_DENIED,
            ErrorCode.ARTIST_NOT_FOUND, ErrorCode.ARTWORK_NOT_FOUND, ErrorCode.ARTWORK_ACCESS_DENIED})
    public ResponseEntity<ApiResponse<Void>> delete(
            @CurrentArtist Artist artist,
            @PathVariable Long artworkId
    ) {
        artworkCommandFacade.delete(artist, artworkId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
