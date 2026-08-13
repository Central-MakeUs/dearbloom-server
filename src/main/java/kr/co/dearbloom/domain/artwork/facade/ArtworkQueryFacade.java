package kr.co.dearbloom.domain.artwork.facade;

import kr.co.dearbloom.domain.artist.entity.artist.Artist;
import kr.co.dearbloom.domain.artwork.dto.ArtworkCursor;
import kr.co.dearbloom.domain.artwork.dto.ArtworkFilterCondition;
import kr.co.dearbloom.domain.artwork.dto.ArtworkPage;
import kr.co.dearbloom.domain.artwork.dto.request.ArtworkQueryRequest;
import kr.co.dearbloom.domain.artwork.dto.response.ArtworkDetailResponse;
import kr.co.dearbloom.domain.artwork.dto.response.ArtworkPageResponse;
import kr.co.dearbloom.domain.artwork.dto.response.ArtworkThumbnailResponse;
import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.domain.artwork.entity.ArtworkPackage;
import kr.co.dearbloom.domain.artwork.entity.PortfolioFile;
import kr.co.dearbloom.domain.artwork.service.ArtworkQueryService;
import kr.co.dearbloom.domain.customer.service.SavedArtworkQueryService;
import kr.co.dearbloom.global.auth.resolver.ViewerContext;
import kr.co.dearbloom.global.util.CursorCodec;
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
    private final CursorCodec cursorCodec;

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

    /**
     * 작품 탐색 목록(필터·정렬·무한스크롤).
     * 다음 페이지 커서는 이번 페이지 마지막 작품의 정렬 키로 만든다.
     * 저장 여부는 지금 페이지에 뜬 작품들로만 좁혀 조회한다.
     */
    @Transactional(readOnly = true)
    public ArtworkPageResponse getArtworkPage(ArtworkQueryRequest request, ViewerContext viewer) {
        ArtworkFilterCondition condition = artworkQueryService.resolveCondition(request);
        ArtworkCursor cursor = cursorCodec.decode(request.getCursor(), ArtworkCursor.class);
        if (cursor != null) {
            cursor.validateFor(condition.sort());
        }

        ArtworkPage page = artworkQueryService.findArtworkPage(condition, cursor, request.getSize());
        List<Artwork> artworks = page.artworks();

        Set<Long> savedArtworkIds = viewer.isCustomer()
                ? savedArtworkQueryService.getSavedArtworkIds(viewer.activeProfileId(), artworkIds(artworks))
                : null;

        return new ArtworkPageResponse(
                artworkQueryService.getSummaries(artworks, savedArtworkIds),
                artworkQueryService.countArtworks(condition),
                page.hasNext() ? cursorCodec.encode(ArtworkCursor.from(artworks.getLast())) : null,
                page.hasNext());
    }

    private List<Long> artworkIds(List<Artwork> artworks) {
        return artworks.stream().map(Artwork::getArtworkId).toList();
    }
}
