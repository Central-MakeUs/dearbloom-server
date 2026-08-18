package kr.co.dearbloom.domain.artist.facade;

import kr.co.dearbloom.domain.artist.dto.artist.request.ArtistTravelFeeUpdateRequest;
import kr.co.dearbloom.domain.artist.dto.artist.request.ArtistIntroUpdateRequest;
import kr.co.dearbloom.domain.artist.dto.artist.request.ArtistRegionUpdateRequest;
import kr.co.dearbloom.domain.artist.dto.artist.response.ArtistDetailResponse;
import kr.co.dearbloom.domain.artist.dto.artist.response.ArtistResponse;
import kr.co.dearbloom.domain.artist.dto.artist.response.NicknameAvailabilityResponse;
import kr.co.dearbloom.domain.artist.entity.artist.Artist;
import kr.co.dearbloom.domain.artist.service.artist.ArtistCommandService;
import kr.co.dearbloom.domain.artist.service.artist.ArtistQueryService;
import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.domain.artwork.event.ArtworkExploreChangedEvent;
import kr.co.dearbloom.global.dto.response.exception.CustomException;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import kr.co.dearbloom.global.file.FileUrlValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 수정 메서드는 모두 @Transactional 이다. 응답 매핑이 LAZY 인 regions 를 읽는데
 * open-in-view: false 라 트랜잭션 안에서 매핑까지 끝내야 하기 때문.
 * <p>
 * 닉네임·활동지역은 작품 카드에 그대로 실려 나가므로 수정하면
 * {@link ArtworkExploreChangedEvent} 로 작품 탐색 첫 화면 캐시를 버린다.
 * 나머지(대표 이미지·소개·출장비)는 카드에 안 나오므로 발행하지 않는다.
 */
@Component
@RequiredArgsConstructor
public class ArtistFacade {
    private final ArtistCommandService artistCommandService;
    private final ArtistQueryService artistQueryService;
    private final FileUrlValidator fileUrlValidator;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public NicknameAvailabilityResponse checkNicknameAvailability(Member member, String nickname) {
        if (!artistQueryService.existsByNickname(nickname)) {
            return new NicknameAvailabilityResponse(true);
        }
        boolean isMine = artistQueryService.findByMember(member)
                .map(artist -> nickname.equals(artist.getNickname()))
                .orElse(false);
        return new NicknameAvailabilityResponse(isMine);
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
        ArtistResponse response = ArtistResponse.from(
                artistCommandService.updateNickname(artist, nickname)
        );
        eventPublisher.publishEvent(new ArtworkExploreChangedEvent()); // 카드의 작가 닉네임
        return response;
    }

    @Transactional
    public ArtistResponse updateIntro(Artist artist, ArtistIntroUpdateRequest request) {
        return ArtistResponse.from(
                artistCommandService.updateIntro(artist, request.getIntro())
        );
    }

    @Transactional
    public ArtistResponse updateRegions(Artist artist, ArtistRegionUpdateRequest request) {
        ArtistResponse response = ArtistResponse.from(
                artistCommandService.updateRegions(artist, request.getRegionList())
        );
        eventPublisher.publishEvent(new ArtworkExploreChangedEvent()); // 카드의 활동지역 칩 + 지역 필터
        return response;
    }

    @Transactional
    public ArtistResponse updateTravelFee(Artist artist, ArtistTravelFeeUpdateRequest request) {
        return ArtistResponse.from(
                artistCommandService.updateTravelFee(artist, request.getTravelFee())
        );
    }
}
