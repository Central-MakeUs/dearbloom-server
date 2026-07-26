package kr.co.dearbloom.domain.artwork.facade;

import kr.co.dearbloom.domain.artist.entity.artist.Artist;
import kr.co.dearbloom.domain.artwork.dto.response.ArtworkDetailResponse;
import kr.co.dearbloom.domain.artwork.dto.response.ArtworkSummaryResponse;
import kr.co.dearbloom.domain.artwork.dto.response.ArtworkThumbnailResponse;
import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.domain.artwork.entity.ArtworkPackage;
import kr.co.dearbloom.domain.artwork.entity.PortfolioFile;
import kr.co.dearbloom.domain.artwork.service.ArtworkQueryService;
import kr.co.dearbloom.domain.customer.service.SavedArtworkQueryService;
import kr.co.dearbloom.global.auth.resolver.ViewerContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ArtworkQueryFacade {
    private final ArtworkQueryService artworkQueryService;
    private final SavedArtworkQueryService savedArtworkQueryService;

    /**
     * 비로그인/고객용 작품 상세 조회.
     * 고객 토큰이면 저장 여부(isSaved)를 채우고, 그 외에는 null.
     */
    @Transactional(readOnly = true)
    public ArtworkDetailResponse getDetail(Long artworkId, ViewerContext viewer) {
        Artwork artwork = artworkQueryService.getById(artworkId);
        Artist artist = artwork.getArtist();
        List<PortfolioFile> files = artworkQueryService.getPortfolioFiles(artwork);
        List<ArtworkPackage> packages = artworkQueryService.getPackages(artwork);
        List<ArtworkThumbnailResponse> otherArtworkList =
                artworkQueryService.getOtherArtworkThumbnails(artist, artworkId);

        Boolean isSaved = viewer.isCustomer()
                ? savedArtworkQueryService.isSaved(viewer.activeProfileId(), artworkId)
                : null;
        return ArtworkDetailResponse.of(artwork, artist, files, packages, otherArtworkList, isSaved);
    }

    // 작품 전체 리스트(최신순). 고객 조회면 각 항목에 저장 여부(isSaved)를 채운다.
    @Transactional(readOnly = true)
    public List<ArtworkSummaryResponse> getArtworkList(ViewerContext viewer) {
        Set<Long> savedArtworkIds = viewer.isCustomer()
                ? savedArtworkQueryService.getSavedArtworkIds(viewer.activeProfileId())
                : null;
        return artworkQueryService.getAllLatestSummaries(savedArtworkIds);
    }
}
