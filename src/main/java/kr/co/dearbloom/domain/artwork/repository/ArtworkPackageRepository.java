package kr.co.dearbloom.domain.artwork.repository;

import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.domain.artwork.entity.ArtworkPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ArtworkPackageRepository extends JpaRepository<ArtworkPackage, Long> {
    List<ArtworkPackage> findByArtwork(Artwork artwork);

    // 여러 작품의 패키지를 한 번에 조회(리스트 최저가 계산용).
    List<ArtworkPackage> findByArtworkIn(Collection<Artwork> artworks);

    void deleteByArtwork(Artwork artwork);
}
