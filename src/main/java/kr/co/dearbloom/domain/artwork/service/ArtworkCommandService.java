package kr.co.dearbloom.domain.artwork.service;

import kr.co.dearbloom.domain.artist.entity.Artist;
import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.domain.artwork.entity.PortfolioFile;
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

    public Artwork create(Artist artist, String title, Integer price) {
        return artworkRepository.save(Artwork.builder()
                .artist(artist)
                .artworkName(title)
                .price(price)
                .build());
    }

    // 사진별 (fileUrl, university) 라벨을 한 번에 저장. 정렬은 sortOrder 로 유지.
    public List<PortfolioFile> savePortfolioFiles(List<PortfolioFile> portfolioFiles) {
        return portfolioFileRepository.saveAll(portfolioFiles);
    }

    // 제목·가격 부분 수정. null 인 항목은 유지.
    public Artwork updateBasicInfo(Artwork artwork, String title, Integer price) {
        artwork.updateBasicInfo(title, price);
        return artworkRepository.save(artwork);
    }

    // 기존 사진을 모두 지우고 받은 목록으로 교체. S3 객체는 건드리지 않는다(DB row 만 교체).
    public List<PortfolioFile> replacePortfolioFiles(Artwork artwork, List<PortfolioFile> newFiles) {
        portfolioFileRepository.deleteByArtwork(artwork);
        return portfolioFileRepository.saveAll(newFiles);
    }

    // 작품 삭제. 사진 row 를 먼저 지운 뒤 작품을 지운다. S3 객체는 건드리지 않는다.
    public void delete(Artwork artwork) {
        portfolioFileRepository.deleteByArtwork(artwork);
        artworkRepository.delete(artwork);
    }
}
