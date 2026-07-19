package kr.co.dearbloom.domain.artist.facade;

import kr.co.dearbloom.domain.artist.dto.request.ArtistCreateRequest;
import kr.co.dearbloom.domain.artist.dto.request.ArtistIntroUpdateRequest;
import kr.co.dearbloom.domain.artist.dto.request.ArtistPricingUpdateRequest;
import kr.co.dearbloom.domain.artist.dto.request.ArtistRegionUpdateRequest;
import kr.co.dearbloom.domain.artist.dto.response.ArtistCreateResponse;
import kr.co.dearbloom.domain.artist.dto.response.ArtistDetailResponse;
import kr.co.dearbloom.domain.artist.dto.response.ArtistResponse;
import kr.co.dearbloom.domain.artist.entity.Artist;
import kr.co.dearbloom.domain.artist.service.ArtistCommandService;
import kr.co.dearbloom.domain.artist.service.ArtistQueryService;
import kr.co.dearbloom.domain.auth.service.TokenService;
import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.domain.member.entity.MemberRole;
import kr.co.dearbloom.domain.member.service.MemberCommandService;
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
    private final MemberCommandService memberCommandService;
    private final TokenService tokenService;
    private final FileUrlValidator fileUrlValidator;

    /**
     * 온보딩. 닉네임·활동 지역·대표 이미지(선택)를 한 번에 받아 작가 프로필을 만든다.
     * 기존 토큰에는 activeProfileId 가 없어 이후 @CurrentArtist API 를 못 쓰므로
     * activeRole 이 ARTIST 로 갱신된 새 accessToken 을 함께 반환한다.
     */
    @Transactional
    public ArtistCreateResponse create(Member member, ArtistCreateRequest request) {
        // 대표 이미지는 선택. 보냈다면 CDN 경로인지 검증한다(빈 문자열도 잘못 보낸 것으로 본다).
        if (request.getImageUrl() != null) {
            fileUrlValidator.validate(request.getImageUrl());
        }
        Member updated = memberCommandService.markAsArtist(member);
        Artist artist = artistCommandService.create(updated, request);
        return new ArtistCreateResponse(
                tokenService.createAccessToken(updated, MemberRole.ARTIST),
                ArtistResponse.from(artist)
        );
    }

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
    public ArtistResponse updatePricing(Artist artist, ArtistPricingUpdateRequest request) {
        return ArtistResponse.from(
                artistCommandService.updatePricing(artist, request)
        );
    }

    @Transactional
    public ArtistResponse deleteTravelFeeInfo(Artist artist) {
        return ArtistResponse.from(
                artistCommandService.deleteTravelFeeInfo(artist)
        );
    }

    @Transactional
    public ArtistResponse deletePackageInfo(Artist artist) {
        return ArtistResponse.from(
                artistCommandService.deletePackageInfo(artist)
        );
    }
}
