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

    // 작품 삭제 시 그 작품에 달린 신고도 함께 정리(FK 제약 위반 방지). 신고 대상이 사라지면 신고도 의미를 잃는다.
    void deleteByArtwork(Artwork artwork);

    // 회원 탈퇴 시 이 회원이 넣은 신고를 정리. 신고자는 Member FK 라 행이 남으면 탈퇴자와 계속 연결된다.
    void deleteByReporter(Member reporter);
}
