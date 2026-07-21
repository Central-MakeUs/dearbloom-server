package kr.co.dearbloom.domain.artwork.repository;

import kr.co.dearbloom.domain.artist.entity.Artist;
import kr.co.dearbloom.domain.artwork.entity.Artwork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArtworkRepository extends JpaRepository<Artwork, Long> {
    List<Artwork> findByArtist(Artist artist);

    // 이 작가의 다른 작품(현재 작품 제외)을 저장 수 많은 순으로 조회.
    List<Artwork> findByArtistAndArtworkIdNotOrderBySavedCountDesc(Artist artist, Long artworkId);

    // 전체 작품을 최신순으로, 작가까지 fetch join(작가 N+1 제거). regions 는 @BatchSize 로 묶임.
    @Query("select a from Artwork a join fetch a.artist order by a.createdAt desc, a.artworkId desc")
    List<Artwork> findAllWithArtistOrderByCreatedAtDesc();

    // 특정 작가의 작품을 최신순으로 조회(작가 fetch join).
    @Query("select a from Artwork a join fetch a.artist where a.artist = :artist"
            + " order by a.createdAt desc, a.artworkId desc")
    List<Artwork> findByArtistWithArtistOrderByCreatedAtDesc(Artist artist);
}
