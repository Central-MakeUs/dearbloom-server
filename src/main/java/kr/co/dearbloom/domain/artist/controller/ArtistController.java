package kr.co.dearbloom.domain.artist.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.dearbloom.domain.artist.dto.request.ArtistProfileImageUpdateRequest;
import kr.co.dearbloom.domain.artist.dto.response.ArtistProfileResponse;
import kr.co.dearbloom.domain.artist.entity.Artist;
import kr.co.dearbloom.domain.artist.facade.ArtistFacade;
import kr.co.dearbloom.global.auth.resolver.CurrentArtist;
import kr.co.dearbloom.global.dto.response.ApiResponse;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import kr.co.dearbloom.global.swagger.ApiErrorCodes;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/artists")
@RequiredArgsConstructor
@Tag(name = "Artist", description = "작가 API")
public class ArtistController {
    private final ArtistFacade artistFacade;

    @PatchMapping("/me/profile/image")
    @Operation(summary = "작가 프로필 이미지 업데이트",
            description = """
                    작가 프로필 이미지를 등록 혹은 수정합니다.<br>
                    presigned 업로드로 받은 CDN fileUrl 을 넘겨주세요.
                    
                    갱신된 작가 프로필을 반환합니다.""")
    @ApiErrorCodes({ErrorCode.INVALID_FILE_URL})
    public ResponseEntity<ApiResponse<ArtistProfileResponse>> updateProfileImage(
            @CurrentArtist Artist artist,
            @RequestBody @Valid ArtistProfileImageUpdateRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                artistFacade.updateProfileImage(artist, request.getProfileImageUrl())
        ));
    }
}
