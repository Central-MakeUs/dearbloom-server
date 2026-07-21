package kr.co.dearbloom.domain.artwork.repository;

import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.domain.artwork.entity.PortfolioFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PortfolioFileRepository extends JpaRepository<PortfolioFile, Long> {
    List<PortfolioFile> findByArtworkOrderBySortOrderAsc(Artwork artwork);

    // 여러 작품의 사진을 sortOrder 오름차순으로 한 번에 조회(대표 이미지 추출용).
    List<PortfolioFile> findByArtworkInOrderBySortOrderAsc(Collection<Artwork> artworks);

    void deleteByArtwork(Artwork artwork);
}
