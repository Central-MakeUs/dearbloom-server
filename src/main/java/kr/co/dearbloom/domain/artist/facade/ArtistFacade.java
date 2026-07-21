package kr.co.dearbloom.domain.artist.facade;

import kr.co.dearbloom.domain.artist.dto.request.ArtistEtcInfoUpdateRequest;
import kr.co.dearbloom.domain.artist.dto.request.ArtistIntroUpdateRequest;
import kr.co.dearbloom.domain.artist.dto.request.ArtistRegionUpdateRequest;
import kr.co.dearbloom.domain.artist.dto.response.ArtistDetailResponse;
import kr.co.dearbloom.domain.artist.dto.response.ArtistResponse;
import kr.co.dearbloom.domain.artist.entity.Artist;
import kr.co.dearbloom.domain.artist.service.ArtistCommandService;
import kr.co.dearbloom.domain.artist.service.ArtistQueryService;
import kr.co.dearbloom.global.dto.response.exception.CustomException;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import kr.co.dearbloom.global.file.FileUrlValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 수정 메서드는 모두 @Transactional 이다. 응답 매핑이 LAZY 인 regions 를 읽는데
 * open-in-view: false 라 트랜잭션 안에서 매핑까지 끝내야 하기 때문.
 */
@Component
@RequiredArgsConstructor
public class ArtistFacade {
    private final ArtistCommandService artistCommandService;
    private final ArtistQueryService artistQueryService;
    private final FileUrlValidator fileUrlValidator;

    // regions 를 함께 조회하므로 매핑 시점에 이미 초기화되어 있다.
    public ArtistDetailResponse getMyInfo(Artist artist) {
        return ArtistDetailResponse.from(
                artistQueryService.getWithRegions(artist.getArtistId())
        );
    }

    @Transactional
    public ArtistResponse updateImage(Artist artist, String imageUrl) {
        fileUrlValidator.validate(imageUrl);
        return ArtistResponse.from(
                artistCommandService.updateImage(artist, imageUrl)
        );
    }

    @Transactional
    public ArtistResponse updateNickname(Artist artist, String nickname) {
        if(artistQueryService.existsByNickname(nickname) && !artist.getNickname().equals(nickname)) {
            throw new CustomException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }
        return ArtistResponse.from(
                artistCommandService.updateNickname(artist, nickname)
        );
    }

    @Transactional
    public ArtistResponse updateIntro(Artist artist, ArtistIntroUpdateRequest request) {
        return ArtistResponse.from(
                artistCommandService.updateIntro(artist, request.getIntro())
        );
    }

    @Transactional
    public ArtistResponse updateRegions(Artist artist, ArtistRegionUpdateRequest request) {
        return ArtistResponse.from(
                artistCommandService.updateRegions(artist, request.getRegionList())
        );
    }

    @Transactional
    public ArtistResponse updateEtcInfo(Artist artist, ArtistEtcInfoUpdateRequest request) {
        return ArtistResponse.from(
                artistCommandService.updateEtcInfo(artist, request.getEtcInfo())
        );
    }
}
