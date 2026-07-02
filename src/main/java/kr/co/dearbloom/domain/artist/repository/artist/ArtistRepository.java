package kr.co.dearbloom.domain.artist.repository.artist;

import kr.co.dearbloom.domain.artist.entity.artist.Artist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, Long> {
}
