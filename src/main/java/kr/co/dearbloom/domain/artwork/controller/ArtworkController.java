package kr.co.dearbloom.domain.artwork.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.dearbloom.domain.artist.entity.Artist;
import kr.co.dearbloom.domain.artwork.dto.request.ArtworkCreateRequest;
import kr.co.dearbloom.domain.artwork.dto.request.ArtworkPhotoUpdateRequest;
import kr.co.dearbloom.domain.artwork.dto.request.ArtworkBasicInfoUpdateRequest;
import kr.co.dearbloom.domain.artwork.dto.response.ArtworkDetailResponse;
import kr.co.dearbloom.domain.artwork.dto.response.ArtworkResponse;
import kr.co.dearbloom.domain.artwork.facade.ArtworkCommandFacade;
import kr.co.dearbloom.domain.artwork.facade.ArtworkQueryFacade;
import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.global.auth.resolver.CurrentArtist;
import kr.co.dearbloom.global.dto.response.ApiResponse;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import kr.co.dearbloom.global.swagger.ApiErrorCodes;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/artworks")
@Tag(name = "Artwork", description = "작품 API")
public class ArtworkController {
    private final ArtworkCommandFacade artworkCommandFacade;
    private final ArtworkQueryFacade artworkQueryFacade;

    @GetMapping("/{artworkId}")
    @Operation(summary = "작품 상세 조회",
            description = """
                    작품 상세를 조회합니다. 로그인하지 않아도 조회할 수 있습니다.<br>
                    작가 본인이 자신의 작품을 조회하면 <b>isMine=true</b> 와 함께 저장 수(savedCount)가 노출됩니다.<br>
                    그 외(비로그인 등)에는 isMine=false, savedCount 는 null 입니다.<br>
                    (고객의 저장 여부는 저장 기능 제공 후 추가될 예정입니다.)
                    """)
    @ApiErrorCodes({ErrorCode.ARTWORK_NOT_FOUND})
    public ResponseEntity<ApiResponse<ArtworkDetailResponse>> getDetail(
            @AuthenticationPrincipal Member member,
            @PathVariable Long artworkId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                artworkQueryFacade.getDetail(artworkId, member)
        ));
    }

    @PostMapping
    @Operation(summary = "작품 등록",
            description = """
                    작품 제목 / 기본 가격과 사진들을 받아 작품을 등록합니다.<br>
                    사진은 1장 이상 필수이며, 각 사진마다 학교 ID 를 1개씩 라벨링할 수 있습니다
                    (학교는 선택이라 null 가능).<br>
                    사진은 등록한 순서대로 정렬됩니다(임시). 각 fileUrl 은 File API 의 presigned URL 로
                    S3 업로드를 완료한 뒤의 CDN URL 이어야 합니다.
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

    @PatchMapping("/{artworkId}")
    @Operation(summary = "작품 기본 정보 수정 (제목/가격)",
            description = """
                    작품의 제목·기본 가격만 부분 수정합니다. <b>사진은 이 API 로 변경되지 않습니다</b>
                    (사진 변경은 사진 교체 API 사용).<br>
                    보내지 않거나 null 인 항목은 그대로 유지됩니다. 본인 작품만 수정할 수 있습니다.<br>
                    응답에는 변경된 기본 정보와 함께 기존 사진 목록도 포함됩니다.
                    """)
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.ROLE_ACCESS_DENIED,
            ErrorCode.ARTIST_NOT_FOUND, ErrorCode.ARTWORK_NOT_FOUND, ErrorCode.ARTWORK_ACCESS_DENIED})
    public ResponseEntity<ApiResponse<ArtworkResponse>> updateBasicInfo(
            @CurrentArtist Artist artist,
            @PathVariable Long artworkId,
            @RequestBody @Valid ArtworkBasicInfoUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                artworkCommandFacade.updateBasicInfo(artist, artworkId, request)
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
