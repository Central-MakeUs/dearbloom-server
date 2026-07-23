package kr.co.dearbloom.domain.artist.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.dearbloom.domain.artist.dto.artist.request.ArtistEtcInfoUpdateRequest;
import kr.co.dearbloom.domain.artist.dto.artist.request.ArtistIntroUpdateRequest;
import kr.co.dearbloom.domain.artist.dto.artist.request.ArtistNicknameUpdateRequest;
import kr.co.dearbloom.domain.artist.dto.artist.request.ArtistImageUpdateRequest;
import kr.co.dearbloom.domain.artist.dto.artist.request.ArtistRegionUpdateRequest;
import kr.co.dearbloom.domain.artist.dto.artist.response.ArtistDetailResponse;
import kr.co.dearbloom.domain.artist.dto.artist.response.ArtistResponse;
import kr.co.dearbloom.domain.artist.entity.artist.Artist;
import kr.co.dearbloom.domain.artist.facade.ArtistFacade;
import kr.co.dearbloom.global.auth.resolver.CurrentArtist;
import kr.co.dearbloom.global.dto.response.ApiResponse;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import kr.co.dearbloom.global.swagger.ApiErrorCodes;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/artists/me")
@RequiredArgsConstructor
@Tag(name = "- Artist -", description = "작가 정보 관리 API")
public class ArtistController {
    private final ArtistFacade artistFacade;

    @GetMapping
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

    @PatchMapping("/image")
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

    @PatchMapping("/nickname")
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

    @PutMapping("/regions")
    @Operation(summary = "작가 활동 지역 수정",
            description = """
                    작가 활동 지역을 수정합니다. 최초 등록은 작가 프로필 생성 API 에서 처리합니다.<br>
                    보낸 값으로 전체 교체되며, 최소 1개 이상 필수입니다.<br><br>
                    <b>regions 가능한 값</b><br>
                    SEOUL, GYEONGGI_NORTH, GYEONGGI_SOUTH, INCHEON, BUSAN, DAEGU, GWANGJU, DAEJEON_SEJONG, ULSAN,
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

    @PatchMapping("/intro")
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

    @PatchMapping("/etc-info")
    @Operation(summary = "작가 기타 안내 수정",
            description = """
                    기타 안내(촬영 취소·환불 규정 등 자유 형식 텍스트)를 수정합니다. 빈 문자열을 보내면 비웁니다.
                    """)
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN,
            ErrorCode.ROLE_ACCESS_DENIED, ErrorCode.ARTIST_NOT_FOUND})
    public ResponseEntity<ApiResponse<ArtistResponse>> updateEtcInfo(
            @CurrentArtist Artist artist,
            @RequestBody @Valid ArtistEtcInfoUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                artistFacade.updateEtcInfo(artist, request)
        ));
    }

}
