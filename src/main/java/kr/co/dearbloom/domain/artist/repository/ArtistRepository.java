package kr.co.dearbloom.domain.artist.repository;

import kr.co.dearbloom.domain.artist.entity.Artist;
import kr.co.dearbloom.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, Long> {
    Optional<Artist> findByMember(Member member);
}
