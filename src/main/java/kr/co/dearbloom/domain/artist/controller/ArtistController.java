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
import kr.co.dearbloom.domain.member.dto.RoleRevokeResponse;
import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.domain.member.facade.MemberFacade;
import kr.co.dearbloom.global.auth.resolver.CurrentArtist;
import kr.co.dearbloom.global.dto.response.ApiResponse;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import kr.co.dearbloom.global.swagger.ApiErrorCodes;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
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
    private final MemberFacade memberFacade;

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

    @DeleteMapping
    @Operation(summary = "작가 역할 해지",
            description = """
                    현재 회원의 <b>작가 역할만</b> 해지합니다(계정 전체 탈퇴가 아님).<br>
                    고객 역할이 함께 있으면 작가 프로필은 익명화되고, <b>남은 고객 역할로 재발급된 accessToken</b> 을
                    응답으로 돌려줍니다 — <code>withdrawn=false</code>. 응답 즉시 기존 accessToken 을 교체하세요(refreshToken 은 유지).<br>
                    작가가 <b>유일한 역할</b>이면 계정 전체가 탈퇴 처리되어 <code>withdrawn=true</code> 로 내려갑니다 —
                    이때는 토큰을 삭제하고 로그인 화면으로 이동하세요.<br>
                    고객 모드로 로그인한 상태에서도 호출할 수 있습니다. 작가 역할이 없으면 403 을 반환합니다.
                    """)
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "작가 역할 해지 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "인증 필요 (토큰 없음/만료/유효하지 않음)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "작가 역할이 없음")
    })
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.ROLE_NOT_AVAILABLE})
    public ResponseEntity<ApiResponse<RoleRevokeResponse>> revokeArtistRole(
            @AuthenticationPrincipal Member member
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                memberFacade.revokeArtistRole(member)
        ));
    }
}
