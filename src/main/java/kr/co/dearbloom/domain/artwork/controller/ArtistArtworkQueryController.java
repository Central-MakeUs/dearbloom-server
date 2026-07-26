package kr.co.dearbloom.domain.artwork.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.dearbloom.domain.artist.entity.artist.Artist;
import kr.co.dearbloom.domain.artwork.dto.response.ArtistArtworkDetailResponse;
import kr.co.dearbloom.domain.artwork.dto.response.ArtistArtworkSummaryResponse;
import kr.co.dearbloom.domain.artwork.facade.ArtistArtworkQueryFacade;
import kr.co.dearbloom.global.auth.resolver.CurrentArtist;
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

/** 작가 본인 작품 조회. (등록/수정/삭제 커맨드는 {@code ArtworkController}) */
@RestController
@RequestMapping("/api/artists/me/artworks")
@RequiredArgsConstructor
@Tag(name = "Artwork - Artist")
public class ArtistArtworkQueryController {
    private final ArtistArtworkQueryFacade artistArtworkQueryFacade;

    @GetMapping
    @Operation(summary = "작가 본인 작품 리스트 조회 (최신순)",
            description = """
                    현재 로그인한 작가가 등록한 작품 전체를 최신 등록순으로 조회합니다.<br>
                    각 항목은 작품 ID / 제목 / 가격 / 촬영 가능 인원 / 작가 닉네임 / 작가 활동지역 / 대표 이미지 / 저장 수 / 조회수입니다.
                    """)
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN,
            ErrorCode.ROLE_ACCESS_DENIED, ErrorCode.ARTIST_NOT_FOUND})
    public ResponseEntity<ApiResponse<List<ArtistArtworkSummaryResponse>>> getMyArtworks(
            @CurrentArtist Artist artist
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                artistArtworkQueryFacade.getArtistArtworkList(artist)
        ));
    }

    @GetMapping("/{artworkId}")
    @Operation(summary = "작가 본인 작품 상세 조회",
            description = """
                    작가 본인 작품의 상세를 조회합니다. 공개 상세 정보(촬영 가능 인원 포함)에 더해
                    <b>저장 수(savedCount) / 조회수(viewCount)</b> 가 포함됩니다.<br>
                    본인 작품만 조회할 수 있습니다.
                    """)
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.ROLE_ACCESS_DENIED,
            ErrorCode.ARTIST_NOT_FOUND, ErrorCode.ARTWORK_NOT_FOUND, ErrorCode.ARTWORK_ACCESS_DENIED})
    public ResponseEntity<ApiResponse<ArtistArtworkDetailResponse>> getMyArtworkDetail(
            @CurrentArtist Artist artist,
            @PathVariable Long artworkId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                artistArtworkQueryFacade.getArtistArtworkDetail(artist, artworkId)
        ));
    }
}
