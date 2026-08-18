package kr.co.dearbloom.domain.artwork.repository;

import kr.co.dearbloom.domain.artist.entity.artist.Artist;
import kr.co.dearbloom.domain.artwork.entity.Artwork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArtworkRepository extends JpaRepository<Artwork, Long> {
    List<Artwork> findByArtist(Artist artist);

    // 특정 작가의 작품을 최신순으로 조회(작가 fetch join).
    @Query("select a from Artwork a join fetch a.artist where a.artist = :artist"
            + " order by a.createdAt desc, a.artworkId desc")
    List<Artwork> findByArtistWithArtistOrderByCreatedAtDesc(Artist artist);
}
