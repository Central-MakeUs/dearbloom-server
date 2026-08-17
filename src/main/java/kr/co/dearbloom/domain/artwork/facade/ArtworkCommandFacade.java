package kr.co.dearbloom.domain.artwork.facade;

import kr.co.dearbloom.domain.artist.entity.artist.Artist;
import kr.co.dearbloom.domain.artwork.dto.request.ArtworkInfoUpdateRequest;
import kr.co.dearbloom.domain.artwork.dto.request.ArtworkCreateRequest;
import kr.co.dearbloom.domain.artwork.dto.request.ArtworkPackageUpdateRequest;
import kr.co.dearbloom.domain.artwork.dto.request.ArtworkPhotoUpdateRequest;
import kr.co.dearbloom.domain.artwork.dto.response.ArtworkResponse;
import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.domain.artwork.entity.ArtworkPackage;
import kr.co.dearbloom.domain.artwork.entity.PortfolioFile;
import kr.co.dearbloom.domain.artwork.event.ArtworkExploreChangedEvent;
import kr.co.dearbloom.domain.artwork.service.ArtworkCommandService;
import kr.co.dearbloom.domain.artwork.service.ArtworkQueryService;
import kr.co.dearbloom.domain.artwork.util.ArtworkPackageFactory;
import kr.co.dearbloom.domain.artwork.util.PortfolioFileFactory;
import kr.co.dearbloom.domain.customer.service.SavedArtworkCommandService;
import kr.co.dearbloom.domain.inquiry.service.InquiryCommandService;
import kr.co.dearbloom.domain.report.service.ReportCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 작품 변경은 모두 작품 탐색 첫 화면 카드에 드러나므로, 각 메서드가
 * {@link ArtworkExploreChangedEvent} 를 발행해 그 화면 캐시를 버린다(커밋 후에 지워진다).
 */
@Component
@RequiredArgsConstructor
public class ArtworkCommandFacade {
    private final ArtworkCommandService artworkCommandService;
    private final ArtworkQueryService artworkQueryService;
    private final PortfolioFileFactory portfolioFileFactory;
    private final ArtworkPackageFactory artworkPackageFactory;
    private final SavedArtworkCommandService savedArtworkCommandService;
    private final InquiryCommandService inquiryCommandService;
    private final ReportCommandService reportCommandService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 작품 등록. 제목·패키지·사진들을 받아 작품을 만든다.
     * 카드에 노출할 가격(패키지 최저가)은 작품 행에 함께 저장한다 — 목록 정렬/필터가 SQL 에서 쓴다.
     * 사진은 각각 (fileUrl, 학교) 로 라벨링되며 학교는 선택(null 가능)이다.
     * 등록 순서를 sortOrder 로 보존한다.
     */
    @Transactional
    public ArtworkResponse create(Artist artist, ArtworkCreateRequest request) {
        Artwork artwork = artworkCommandService.create(artist, request.getTitle(),
                request.getMinHeadCount(), request.getMaxHeadCount(),
                artworkPackageFactory.lowestPrice(request.getPackageList()));
        List<ArtworkPackage> packages = artworkCommandService.savePackages(
                artworkPackageFactory.create(artwork, request.getPackageList()));
        List<PortfolioFile> files = artworkCommandService.savePortfolioFiles(
                portfolioFileFactory.create(artwork, request.getPhotoList()));
        eventPublisher.publishEvent(new ArtworkExploreChangedEvent()); // 새 작품이 최신순 첫 자리에 온다
        return ArtworkResponse.of(artwork, packages, files);
    }

    /**
     * 작품명·설명 부분 수정. 각 필드 null 이면 그대로 둔다. 사진·패키지는 건드리지 않는다.
     * 소유권을 검증한 뒤 패키지·사진까지 포함한 전체를 돌려준다.
     */
    @Transactional
    public ArtworkResponse updateBasicInfo(Artist artist, Long artworkId, ArtworkInfoUpdateRequest request) {
        Artwork artwork = artworkQueryService.getOwnedBy(artworkId, artist);
        artworkCommandService.updateBasicInfo(artwork, request.getTitle(), request.getDescription());
        eventPublisher.publishEvent(new ArtworkExploreChangedEvent()); // 카드의 작품명
        return ArtworkResponse.of(artwork, artworkQueryService.getPackages(artwork),
                artworkQueryService.getPortfolioFiles(artwork));
    }

    /**
     * 사진 전체 교체. 받은 목록으로 기존 사진 row 를 통째로 갈아끼운다(S3 재업로드 아님).
     * 유지할 사진은 기존 CDN URL 을 그대로 다시 보내면 되고, 신규 사진만 새로 업로드된다.
     */
    @Transactional
    public ArtworkResponse replacePhotos(Artist artist, Long artworkId, ArtworkPhotoUpdateRequest request) {
        Artwork artwork = artworkQueryService.getOwnedBy(artworkId, artist);
        List<PortfolioFile> replacedPortfolioFiles = artworkCommandService.replacePortfolioFiles(
                artwork, portfolioFileFactory.create(artwork, request.getPhotoList()));
        eventPublisher.publishEvent(new ArtworkExploreChangedEvent()); // 카드의 thumbnailUrl·photoList
        return ArtworkResponse.of(artwork, artworkQueryService.getPackages(artwork), replacedPortfolioFiles);
    }

    /**
     * 패키지 전체 교체. 받은 목록으로 기존 패키지 row 를 통째로 갈아끼우고 작품의 가격(최저가)을 다시 계산한다.
     * <p>
     * 지우기 전에 그 작품의 문의들이 들고 있는 패키지 참조를 먼저 끊는다 — 안 끊으면 FK 제약에 걸린다.
     * 문의는 작가·작품 참조를 따로 들고 있어서, 패키지가 끊겨도 작가 문의함·예약 확정 슬롯 계산·작품 상세
     * 이동이 전부 그대로 동작한다. 합의된 조건(패키지명·가격·소요시간·보정본 수)은 문의 스냅샷에 남는다.
     */
    @Transactional
    public ArtworkResponse replacePackages(Artist artist, Long artworkId, ArtworkPackageUpdateRequest request) {
        Artwork artwork = artworkQueryService.getOwnedBy(artworkId, artist);
        inquiryCommandService.detachArtworkPackages(artwork);
        List<ArtworkPackage> replacedPackages = artworkCommandService.replacePackages(
                artwork,
                artworkPackageFactory.create(artwork, request.getPackageList()),
                artworkPackageFactory.lowestPrice(request.getPackageList()));
        eventPublisher.publishEvent(new ArtworkExploreChangedEvent()); // 카드의 가격(최저가) + 가격 정렬
        return ArtworkResponse.of(artwork, replacedPackages, artworkQueryService.getPortfolioFiles(artwork));
    }

    /**
     * 작품 삭제. 소유권을 검증한 뒤 작품·패키지·사진을 함께 지운다.
     * 이 작품을 참조하는 다른 도메인 행도 먼저 정리해야 FK 제약에 걸리지 않는다 —
     * 저장(SavedArtwork)은 삭제하고, 문의(Inquiry)는 스냅샷으로 기록을 남겨야 하므로 패키지 참조만 끊는다.
     * S3 객체는 건드리지 않는다(DB row 만 삭제).
     */
    @Transactional
    public void delete(Artist artist, Long artworkId) {
        Artwork artwork = artworkQueryService.getOwnedBy(artworkId, artist);
        savedArtworkCommandService.deleteByArtwork(artwork);
        reportCommandService.deleteByArtwork(artwork);
        inquiryCommandService.detachArtwork(artwork);
        artworkCommandService.delete(artwork);
        eventPublisher.publishEvent(new ArtworkExploreChangedEvent()); // 목록에서 빠지고 totalCount 가 줄어든다
    }
}
