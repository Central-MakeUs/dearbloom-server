package kr.co.dearbloom.domain.artwork.repository;

import kr.co.dearbloom.domain.artist.entity.artist.Artist;
import kr.co.dearbloom.domain.artwork.entity.Artwork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArtworkRepository extends JpaRepository<Artwork, Long> {
    /**
     * 조회수에 증가분을 더한다. Redis 에 쌓인 값을 옮길 때만 쓴다.
     * <p>
     * 절대값을 덮어쓰지 않고 더하는 이유 — 진실을 DB 에 두기 위해서다(자세한 건
     * {@code ArtworkViewCountRepository} javadoc). NULL 인 기존 행도 0 으로 보고 더한다.
     *
     * @return 갱신된 행 수. 0 이면 그 사이 작품이 삭제된 것이다
     */
    @Modifying
    @Query("update Artwork a set a.viewCount = coalesce(a.viewCount, 0) + :delta"
            + " where a.artworkId = :artworkId")
    int increaseViewCount(@Param("artworkId") Long artworkId, @Param("delta") long delta);

    List<Artwork> findByArtist(Artist artist);

    // 특정 작가의 작품을 최신순으로 조회(작가 fetch join).
    @Query("select a from Artwork a join fetch a.artist where a.artist = :artist"
            + " order by a.createdAt desc, a.artworkId desc")
    List<Artwork> findByArtistWithArtistOrderByCreatedAtDesc(Artist artist);
}
