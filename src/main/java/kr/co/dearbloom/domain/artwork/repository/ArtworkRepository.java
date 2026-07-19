package kr.co.dearbloom.domain.artwork.repository;

import kr.co.dearbloom.domain.artist.entity.Artist;
import kr.co.dearbloom.domain.artwork.entity.Artwork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArtworkRepository extends JpaRepository<Artwork, Long> {
    List<Artwork> findByArtist(Artist artist);

    // 이 작가의 다른 작품(현재 작품 제외)을 저장 수 많은 순으로 조회.
    List<Artwork> findByArtistAndArtworkIdNotOrderBySavedCountDesc(Artist artist, Long artworkId);
}
