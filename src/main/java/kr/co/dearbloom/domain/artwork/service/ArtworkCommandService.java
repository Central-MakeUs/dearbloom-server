package kr.co.dearbloom.domain.artwork.service;

import kr.co.dearbloom.domain.artist.entity.Artist;
import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.domain.artwork.entity.ArtworkPackage;
import kr.co.dearbloom.domain.artwork.entity.PortfolioFile;
import kr.co.dearbloom.domain.artwork.repository.ArtworkPackageRepository;
import kr.co.dearbloom.domain.artwork.repository.ArtworkRepository;
import kr.co.dearbloom.domain.artwork.repository.PortfolioFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ArtworkCommandService {
    private final ArtworkRepository artworkRepository;
    private final PortfolioFileRepository portfolioFileRepository;
    private final ArtworkPackageRepository artworkPackageRepository;

    public Artwork create(Artist artist, String title, Integer minHeadCount, Integer maxHeadCount) {
        return artworkRepository.save(Artwork.builder()
                .artist(artist)
                .artworkName(title)
                .minHeadCount(minHeadCount)
                .maxHeadCount(maxHeadCount)
                .build());
    }

    // 사진별 (fileUrl, university) 라벨을 한 번에 저장. 정렬은 sortOrder 로 유지.
    public List<PortfolioFile> savePortfolioFiles(List<PortfolioFile> portfolioFiles) {
        return portfolioFileRepository.saveAll(portfolioFiles);
    }

    // 패키지들을 한 번에 저장.
    public List<ArtworkPackage> savePackages(List<ArtworkPackage> packages) {
        return artworkPackageRepository.saveAll(packages);
    }

    // 제목 부분 수정. null 이면 유지.
    public Artwork updateTitle(Artwork artwork, String title) {
        artwork.updateTitle(title);
        return artworkRepository.save(artwork);
    }

    // 기존 사진을 모두 지우고 받은 목록으로 교체. S3 객체는 건드리지 않는다(DB row 만 교체).
    public List<PortfolioFile> replacePortfolioFiles(Artwork artwork, List<PortfolioFile> newFiles) {
        portfolioFileRepository.deleteByArtwork(artwork);
        return portfolioFileRepository.saveAll(newFiles);
    }

    // 작품 삭제. 패키지·사진 row 를 먼저 지운 뒤 작품을 지운다. S3 객체는 건드리지 않는다.
    public void delete(Artwork artwork) {
        artworkPackageRepository.deleteByArtwork(artwork);
        portfolioFileRepository.deleteByArtwork(artwork);
        artworkRepository.delete(artwork);
    }
}
