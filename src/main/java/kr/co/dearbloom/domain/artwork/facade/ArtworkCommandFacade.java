package kr.co.dearbloom.domain.artwork.facade;

import kr.co.dearbloom.domain.artist.entity.Artist;
import kr.co.dearbloom.domain.artwork.dto.request.ArtworkTitleUpdateRequest;
import kr.co.dearbloom.domain.artwork.dto.request.ArtworkCreateRequest;
import kr.co.dearbloom.domain.artwork.dto.request.ArtworkPhotoUpdateRequest;
import kr.co.dearbloom.domain.artwork.dto.response.ArtworkResponse;
import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.domain.artwork.entity.ArtworkPackage;
import kr.co.dearbloom.domain.artwork.entity.PortfolioFile;
import kr.co.dearbloom.domain.artwork.service.ArtworkCommandService;
import kr.co.dearbloom.domain.artwork.service.ArtworkQueryService;
import kr.co.dearbloom.domain.artwork.util.ArtworkPackageFactory;
import kr.co.dearbloom.domain.artwork.util.PortfolioFileFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ArtworkCommandFacade {
    private final ArtworkCommandService artworkCommandService;
    private final ArtworkQueryService artworkQueryService;
    private final PortfolioFileFactory portfolioFileFactory;
    private final ArtworkPackageFactory artworkPackageFactory;

    /**
     * 작품 등록. 제목·기본 가격과 사진들을 받아 작품을 만든다.
     * 사진은 각각 (fileUrl, 학교) 로 라벨링되며 학교는 선택(null 가능)이다.
     * 등록 순서를 sortOrder 로 보존한다.
     */
    @Transactional
    public ArtworkResponse create(Artist artist, ArtworkCreateRequest request) {
        Artwork artwork = artworkCommandService.create(artist, request.getTitle(),
                request.getMinHeadCount(), request.getMaxHeadCount());
        List<ArtworkPackage> packages = artworkCommandService.savePackages(
                artworkPackageFactory.create(artwork, request.getPackageList()));
        List<PortfolioFile> files = artworkCommandService.savePortfolioFiles(
                portfolioFileFactory.create(artwork, request.getPhotoList()));
        return ArtworkResponse.of(artwork, packages, files);
    }

    /**
     * 제목 부분 수정. null 이면 그대로 둔다. 사진·패키지는 건드리지 않는다.
     * 소유권을 검증한 뒤 패키지·사진까지 포함한 전체를 돌려준다.
     */
    @Transactional
    public ArtworkResponse updateTitle(Artist artist, Long artworkId, ArtworkTitleUpdateRequest request) {
        Artwork artwork = artworkQueryService.getOwnedBy(artworkId, artist);
        artworkCommandService.updateTitle(artwork, request.getTitle());
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
        return ArtworkResponse.of(artwork, artworkQueryService.getPackages(artwork), replacedPortfolioFiles);
    }

    /**
     * 작품 삭제. 소유권을 검증한 뒤 작품과 사진을 함께 지운다.
     * S3 객체는 건드리지 않는다(DB row 만 삭제).
     */
    @Transactional
    public void delete(Artist artist, Long artworkId) {
        Artwork artwork = artworkQueryService.getOwnedBy(artworkId, artist);
        artworkCommandService.delete(artwork);
    }
}
