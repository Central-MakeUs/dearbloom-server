package kr.co.dearbloom.domain.report.repository;

import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.domain.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    boolean existsByReporterAndArtwork(Member reporter, Artwork artwork);

    boolean existsByReporter_MemberIdAndArtwork_ArtworkId(Long memberId, Long artworkId);
}
