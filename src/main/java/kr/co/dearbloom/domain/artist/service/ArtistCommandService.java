package kr.co.dearbloom.domain.artist.service;

import kr.co.dearbloom.domain.artist.dto.request.ArtistCreateRequest;
import kr.co.dearbloom.domain.artist.dto.request.ArtistPricingUpdateRequest;
import kr.co.dearbloom.domain.artist.entity.Artist;
import kr.co.dearbloom.domain.artist.entity.Region;
import kr.co.dearbloom.domain.artist.repository.ArtistRepository;
import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.global.dto.response.exception.CustomException;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Transactional
@Service
@RequiredArgsConstructor
public class ArtistCommandService {
    private final ArtistRepository artistRepository;

    // 온보딩 3개 항목을 한 번에 받아 작가 프로필을 만든다. 한 Member 당 하나만 허용.
    public Artist create(Member member, ArtistCreateRequest request) {
        if (artistRepository.findByMember(member).isPresent()) {
            throw new CustomException(ErrorCode.ARTIST_ALREADY_EXISTS);
        }
        return artistRepository.save(Artist.builder()
                .member(member)
                .nickname(request.getNickname())
                .imageUrl(request.getImageUrl())
                .regions(new HashSet<>(request.getRegionList()))
                .build());
    }

    public Artist updateImage(Artist artist, String imageUrl) {
        artist.updateImageUrl(imageUrl);
        return artistRepository.save(artist);
        // @CurrentArtist 로 넘어온 엔티티는 detached 일 수 있어 명시 저장(merge)
    }

    public Artist updateNickname(Artist artist, String nickname) {
        artist.updateNickname(nickname);
        return artistRepository.save(artist);
    }

    public Artist updateIntro(Artist artist, String intro) {
        artist.updateIntro(intro);
        return artistRepository.save(artist);
    }

    public Artist updateRegions(Artist artist, Set<Region> regions) {
        // regions 가 LAZY 라 detached 상태에서 건드리면 터진다. merge 로 관리 상태를 먼저 확보.
        Artist managed = artistRepository.save(artist);
        managed.updateRegions(regions);
        return managed;
    }

    public Artist updatePricing(Artist artist, ArtistPricingUpdateRequest request) {
        artist.updatePricing(request.getTravelFeeInfo(), request.getPackageInfo());
        return artistRepository.save(artist);
    }

    public Artist deleteTravelFeeInfo(Artist artist) {
        artist.deleteTravelFeeInfo();
        return artistRepository.save(artist);
    }

    public Artist deletePackageInfo(Artist artist) {
        artist.deletePackageInfo();
        return artistRepository.save(artist);
    }
}
