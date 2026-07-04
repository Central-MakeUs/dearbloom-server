package kr.co.dearbloom.domain.board.repository.candidate;

import kr.co.dearbloom.domain.board.entity.candidate.CandidateComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CandidateCommentRepository extends JpaRepository<CandidateComment, Long> {
}
