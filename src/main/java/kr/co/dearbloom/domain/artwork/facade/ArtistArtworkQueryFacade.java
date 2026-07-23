package kr.co.dearbloom.domain.artwork.facade;

import kr.co.dearbloom.domain.artist.entity.artist.Artist;
import kr.co.dearbloom.domain.artwork.dto.response.ArtistArtworkDetailResponse;
import kr.co.dearbloom.domain.artwork.dto.response.ArtistArtworkSummaryResponse;
import kr.co.dearbloom.domain.artwork.dto.response.ArtworkThumbnailResponse;
import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.domain.artwork.entity.ArtworkPackage;
import kr.co.dearbloom.domain.artwork.entity.PortfolioFile;
import kr.co.dearbloom.domain.artwork.service.ArtworkQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 작가 본인 작품 조회. 비로그인/고객 조회는 {@code ArtworkQueryFacade}. */
@Component
@RequiredArgsConstructor
public class ArtistArtworkQueryFacade {
    private final ArtworkQueryService artworkQueryService;

    /** 작가 본인의 작품 리스트(최신순). 저장 수/조회수 포함. */
    @Transactional(readOnly = true)
    public List<ArtistArtworkSummaryResponse> getArtistArtworkList(Artist artist) {
        return artworkQueryService.getArtistArtworkSummaries(artist);
    }

    /** 작가 본인용 작품 상세 조회. 소유권을 검증하고 저장 수/조회수를 포함한다. */
    @Transactional(readOnly = true)
    public ArtistArtworkDetailResponse getArtistArtworkDetail(Artist artist, Long artworkId) {
        Artwork artwork = artworkQueryService.getOwnedBy(artworkId, artist);
        // @CurrentArtist 로 들어온 artist 는 detached — 트랜잭션 안의 managed 엔티티를 쓴다(LAZY regions 초기화).
        Artist managedArtist = artwork.getArtist();
        List<PortfolioFile> files = artworkQueryService.getPortfolioFiles(artwork);
        List<ArtworkPackage> packages = artworkQueryService.getPackages(artwork);
        List<ArtworkThumbnailResponse> otherArtworkList =
                artworkQueryService.getOtherArtworkThumbnails(managedArtist, artworkId);
        return ArtistArtworkDetailResponse.of(artwork, managedArtist, files, packages, otherArtworkList);
    }
}
