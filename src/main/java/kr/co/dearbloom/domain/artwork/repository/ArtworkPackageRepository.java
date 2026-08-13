package kr.co.dearbloom.domain.artwork.repository;

import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.domain.artwork.entity.ArtworkPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArtworkPackageRepository extends JpaRepository<ArtworkPackage, Long> {
    List<ArtworkPackage> findByArtwork(Artwork artwork);

    void deleteByArtwork(Artwork artwork);
}
