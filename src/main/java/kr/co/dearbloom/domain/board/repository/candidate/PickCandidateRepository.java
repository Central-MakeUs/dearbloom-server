package kr.co.dearbloom.domain.board.repository.candidate;

import kr.co.dearbloom.domain.board.entity.candidate.PickCandidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PickCandidateRepository extends JpaRepository<PickCandidate, Long> {
}
