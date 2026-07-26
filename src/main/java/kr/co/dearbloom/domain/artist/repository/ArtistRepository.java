package kr.co.dearbloom.domain.artist.repository;

import kr.co.dearbloom.domain.artist.entity.artist.Artist;
import kr.co.dearbloom.domain.member.entity.Member;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, Long> {
    Optional<Artist> findByMember(Member member);

    // regions 가 LAZY 라 조회 응답 매핑 전에 함께 가져온다.
    @EntityGraph(attributePaths = "regions")
    Optional<Artist> findWithRegionsByArtistId(Long artistId);

    boolean existsByNickname(String nickname);
}
