package kr.co.dearbloom.domain.artwork.facade;

import kr.co.dearbloom.domain.artist.entity.artist.Artist;
import kr.co.dearbloom.domain.artwork.dto.ArtworkCursor;
import kr.co.dearbloom.domain.artwork.dto.ArtworkFilterCondition;
import kr.co.dearbloom.domain.artwork.dto.ArtworkPage;
import kr.co.dearbloom.domain.artwork.dto.request.ArtworkQueryRequest;
import kr.co.dearbloom.domain.artwork.dto.response.ArtworkDetailResponse;
import kr.co.dearbloom.domain.artwork.dto.response.ArtworkPageResponse;
import kr.co.dearbloom.domain.artwork.dto.response.ArtworkSummaryResponse;
import kr.co.dearbloom.domain.artwork.dto.response.ArtworkThumbnailResponse;
import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.domain.artwork.entity.ArtworkPackage;
import kr.co.dearbloom.domain.artwork.entity.PortfolioFile;
import kr.co.dearbloom.domain.artwork.repository.ArtworkExploreCacheRepository;
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
    private final ArtworkExploreCacheRepository artworkExploreCacheRepository;
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
     * <p>
     * 파라미터가 하나도 없는 첫 진입 화면만 Redis 캐시를 탄다. 캐시에는 사람마다 다른 isSaved 를 담지 않으므로,
     * 캐시든 DB 든 똑같이 조회 시점에 저장 여부를 덧씌운다 — 그래서 로그인 사용자도 캐시를 탄다.
     */
    @Transactional(readOnly = true)
    public ArtworkPageResponse getArtworkPage(ArtworkQueryRequest request, ViewerContext viewer) {
        ArtworkPageResponse page = sharedPage(request);
        if (!viewer.isCustomer() || page.artworkList().isEmpty()) {
            return page;
        }
        Set<Long> savedArtworkIds = savedArtworkQueryService.getSavedArtworkIds(
                viewer.activeProfileId(),
                page.artworkList().stream().map(ArtworkSummaryResponse::artworkId).toList());

        return new ArtworkPageResponse(
                page.artworkList().stream()
                        .map(artwork -> artwork.withSaved(savedArtworkIds.contains(artwork.artworkId())))
                        .toList(),
                page.totalCount(),
                page.nextCursor(),
                page.hasNext());
    }

    /** 사람과 무관한(isSaved 가 전부 null 인) 페이지. 첫 화면이면 캐시에서, 아니면 DB 에서 가져온다. */
    private ArtworkPageResponse sharedPage(ArtworkQueryRequest request) {
        if (!request.isFirstScreen()) {
            return loadPage(request);
        }
        return artworkExploreCacheRepository.find().orElseGet(() -> {
            ArtworkPageResponse loaded = loadPage(request);
            artworkExploreCacheRepository.save(loaded);
            return loaded;
        });
    }

    private ArtworkPageResponse loadPage(ArtworkQueryRequest request) {
        ArtworkFilterCondition condition = artworkQueryService.resolveCondition(request);
        ArtworkCursor cursor = cursorCodec.decode(request.getCursor(), ArtworkCursor.class);
        if (cursor != null) {
            cursor.validateFor(condition.sort());
        }

        ArtworkPage page = artworkQueryService.findArtworkPage(condition, cursor, ArtworkQueryRequest.PAGE_SIZE);
        List<Artwork> artworks = page.artworks();

        return new ArtworkPageResponse(
                artworkQueryService.getSummaries(artworks, null), // isSaved 는 아래 fillSavedFlags 가 채운다
                artworkQueryService.countArtworks(condition),
                page.hasNext() ? cursorCodec.encode(ArtworkCursor.from(artworks.getLast())) : null,
                page.hasNext());
    }

    /** 고객 조회면 이 페이지에 뜬 작품들의 저장 여부를 채운다. 그 외(비로그인·작가)는 null 그대로 둔다. */
    private ArtworkPageResponse fillSavedFlags(ArtworkPageResponse page, ViewerContext viewer) {
        if (!viewer.isCustomer() || page.artworkList().isEmpty()) {
            return page;
        }
        Set<Long> savedArtworkIds = savedArtworkQueryService.getSavedArtworkIds(
                viewer.activeProfileId(),
                page.artworkList().stream().map(ArtworkSummaryResponse::artworkId).toList());

        return new ArtworkPageResponse(
                page.artworkList().stream()
                        .map(artwork -> artwork.withSaved(savedArtworkIds.contains(artwork.artworkId())))
                        .toList(),
                page.totalCount(),
                page.nextCursor(),
                page.hasNext());
    }
}
