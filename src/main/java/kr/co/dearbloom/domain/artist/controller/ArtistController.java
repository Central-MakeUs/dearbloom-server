package kr.co.dearbloom.domain.artist.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.dearbloom.domain.artist.dto.request.ArtistCreateRequest;
import kr.co.dearbloom.domain.artist.dto.request.ArtistIntroUpdateRequest;
import kr.co.dearbloom.domain.artist.dto.request.ArtistNicknameUpdateRequest;
import kr.co.dearbloom.domain.artist.dto.request.ArtistPricingUpdateRequest;
import kr.co.dearbloom.domain.artist.dto.request.ArtistImageUpdateRequest;
import kr.co.dearbloom.domain.artist.dto.request.ArtistRegionUpdateRequest;
import kr.co.dearbloom.domain.artist.dto.response.ArtistCreateResponse;
import kr.co.dearbloom.domain.artist.dto.response.ArtistDetailResponse;
import kr.co.dearbloom.domain.artist.dto.response.ArtistResponse;
import kr.co.dearbloom.domain.artist.entity.Artist;
import kr.co.dearbloom.domain.artist.facade.ArtistFacade;
import kr.co.dearbloom.domain.artwork.dto.response.ArtistArtworkDetailResponse;
import kr.co.dearbloom.domain.artwork.dto.response.ArtistArtworkSummaryResponse;
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

import java.util.List;

@RestController
@RequestMapping("/api/artists")
@RequiredArgsConstructor
@Tag(name = "Artist", description = "작가 API")
public class ArtistController {
    private final ArtistFacade artistFacade;
    private final ArtworkQueryFacade artworkQueryFacade;

    @PostMapping
    @Operation(summary = "작가 계정 생성 (온보딩)",
            description = """
                    닉네임 / 활동 지역 / 대표 이미지를 한 번에 받아 작가 프로필을 생성합니다.<br>
                    닉네임과 활동 지역은 필수, <b>대표 이미지는 선택</b>입니다 — 보내지 않으면 이미지 없이 생성되며
                    이후 대표 이미지 수정 API 로 등록할 수 있습니다.<br>
                    회원가입 직후의 토큰에는 작가 프로필이 없으므로, 이 API 는
                    <b>activeRole 이 ARTIST 로 갱신된 새 accessToken</b> 을 함께 반환합니다 <br>
                    — 응답받는 즉시 기존 accessToken 을 교체해야 이후 작가 API 를 호출할 수 있습니다.<br>
                    refreshToken 은 재발급하지 않으며 그대로 사용합니다.<br>
                    이미 작가 프로필이 있으면 409 를 반환합니다.<br><br>
                    <b>regions 가능한 값</b><br>
                    SEOUL, GYEONGGI, INCHEON, BUSAN, DAEGU, GWANGJU, DAEJEON, ULSAN, SEJONG,
                    GANGWON, CHUNGBUK, CHUNGNAM, JEONBUK, JEONNAM, GYEONGBUK, GYEONGNAM, JEJU
                    """)
    @ApiErrorCodes({ErrorCode.EXPIRED_TOKEN, ErrorCode.INVALID_FILE_URL, ErrorCode.ARTIST_ALREADY_EXISTS})
    public ResponseEntity<ApiResponse<ArtistCreateResponse>> create(
            @AuthenticationPrincipal Member member,
            @RequestBody @Valid ArtistCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                artistFacade.create(member, request)
        ));
    }

    @GetMapping("/me")
    @Operation(summary = "작가 정보 조회",
            description = """
                    현재 로그인한 작가의 프로필(닉네임 / 프로필 이미지),
                    작가 정보(소개 / 활동 지역), 촬영 정보(출장비 / 패키지)를 조회합니다.
                    """)
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN,
            ErrorCode.ROLE_ACCESS_DENIED, ErrorCode.ARTIST_NOT_FOUND})
    public ResponseEntity<ApiResponse<ArtistDetailResponse>> getMyInfo(
            @CurrentArtist Artist artist
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                artistFacade.getMyInfo(artist)
        ));
    }

    @GetMapping("/me/artworks")
    @Operation(summary = "작가 본인 작품 리스트 조회 (최신순)",
            description = """
                    현재 로그인한 작가가 등록한 작품 전체를 최신 등록순으로 조회합니다.<br>
                    각 항목은 작품 ID / 제목 / 가격 / 작가 닉네임 / 작가 활동지역 / 대표 이미지 / 저장 수 / 조회수입니다.
                    """)
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN,
            ErrorCode.ROLE_ACCESS_DENIED, ErrorCode.ARTIST_NOT_FOUND})
    public ResponseEntity<ApiResponse<List<ArtistArtworkSummaryResponse>>> getMyArtworks(
            @CurrentArtist Artist artist
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                artworkQueryFacade.getArtistArtworkList(artist)
        ));
    }

    @GetMapping("/me/artworks/{artworkId}")
    @Operation(summary = "작가 본인 작품 상세 조회",
            description = """
                    작가 본인 작품의 상세를 조회합니다. 공개 상세 정보에 더해
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
                artworkQueryFacade.getArtistArtworkDetail(artist, artworkId)
        ));
    }

    @PatchMapping("/me/image")
    @Operation(summary = "작가 대표 이미지 수정",
            description = """
                    작가 대표 이미지를 수정합니다. 최초 등록은 작가 프로필 생성 API 에서 처리합니다.<br>
                    File API로 받은 presigned url로 S3에 이미지를 업로드 완료 후 fileUrl을 넘겨주세요.
                    """)
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.INVALID_FILE_URL,
            ErrorCode.ROLE_ACCESS_DENIED, ErrorCode.ARTIST_NOT_FOUND})
    public ResponseEntity<ApiResponse<ArtistResponse>> updateImage(
            @CurrentArtist Artist artist,
            @RequestBody @Valid ArtistImageUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                artistFacade.updateImage(artist, request.getArtistImageUrl())
        ));
    }

    @PatchMapping("/me/nickname")
    @Operation(summary = "작가 닉네임 수정",
            description = """
                    작가 닉네임을 수정합니다. 최초 등록은 작가 프로필 생성 API 에서 처리합니다.<br>
                    닉네임은 2-12자의 한글, 영문, 숫자, _ 만 사용할 수 있습니다.
                    """)
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN,
            ErrorCode.ROLE_ACCESS_DENIED, ErrorCode.ARTIST_NOT_FOUND})
    public ResponseEntity<ApiResponse<ArtistResponse>> updateNickname(
            @CurrentArtist Artist artist,
            @RequestBody @Valid ArtistNicknameUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                artistFacade.updateNickname(artist, request.getNickname())
        ));
    }

    @PutMapping("/me/regions")
    @Operation(summary = "작가 활동 지역 수정",
            description = """
                    작가 활동 지역을 수정합니다. 최초 등록은 작가 프로필 생성 API 에서 처리합니다.<br>
                    보낸 값으로 전체 교체되며, 최소 1개 이상 필수입니다.<br><br>
                    <b>regions 가능한 값</b><br>
                    SEOUL, GYEONGGI, INCHEON, BUSAN, DAEGU, GWANGJU, DAEJEON, ULSAN, SEJONG,
                    GANGWON, CHUNGBUK, CHUNGNAM, JEONBUK, JEONNAM, GYEONGBUK, GYEONGNAM, JEJU
                    """)
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN,
            ErrorCode.ROLE_ACCESS_DENIED, ErrorCode.ARTIST_NOT_FOUND})
    public ResponseEntity<ApiResponse<ArtistResponse>> updateRegions(
            @CurrentArtist Artist artist,
            @RequestBody @Valid ArtistRegionUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                artistFacade.updateRegions(artist, request)
        ));
    }

    @PatchMapping("/me/intro")
    @Operation(summary = "작가 소개 수정",
            description = """
                    작가 소개를 수정합니다. 빈 문자열을 보내면 소개를 비웁니다.
                    """)
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN,
            ErrorCode.ROLE_ACCESS_DENIED, ErrorCode.ARTIST_NOT_FOUND})
    public ResponseEntity<ApiResponse<ArtistResponse>> updateIntro(
            @CurrentArtist Artist artist,
            @RequestBody @Valid ArtistIntroUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                artistFacade.updateIntro(artist, request)
        ));
    }

    @PatchMapping("/me/pricing")
    @Operation(summary = "촬영 정보 업데이트",
            description = """
                    출장비 / 패키지 정보를 한 번에 수정합니다.<br>
                    보내지 않거나 null 인 항목은 변경하지 않습니다.<br>
                    travelFeeInfo, packageInfo 는 줄바꿈이 포함된 자유 형식 텍스트입니다.
                    """)
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN,
            ErrorCode.ROLE_ACCESS_DENIED, ErrorCode.ARTIST_NOT_FOUND})
    public ResponseEntity<ApiResponse<ArtistResponse>> updatePricing(
            @CurrentArtist Artist artist,
            @RequestBody @Valid ArtistPricingUpdateRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                artistFacade.updatePricing(artist, request)
        ));
    }

    @DeleteMapping("/me/pricing/travel-fee")
    @Operation(summary = "출장비 삭제",
            description = """
                    출장비 정보를 삭제합니다. 출장비만 비워지고 다른 정보는 그대로 유지됩니다.
                    """)
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN,
            ErrorCode.ROLE_ACCESS_DENIED, ErrorCode.ARTIST_NOT_FOUND})
    public ResponseEntity<ApiResponse<ArtistResponse>> deleteTravelFeeInfo(
            @CurrentArtist Artist artist
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                artistFacade.deleteTravelFeeInfo(artist)
        ));
    }

    @DeleteMapping("/me/pricing/package")
    @Operation(summary = "패키지 정보 삭제",
            description = """
                    패키지 정보를 삭제합니다. 패키지 정보만 비워지고 다른 정보는 그대로 유지됩니다.
                    """)
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN,
            ErrorCode.ROLE_ACCESS_DENIED, ErrorCode.ARTIST_NOT_FOUND})
    public ResponseEntity<ApiResponse<ArtistResponse>> deletePackageInfo(
            @CurrentArtist Artist artist
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                artistFacade.deletePackageInfo(artist)
        ));
    }
}
