package kr.co.dearbloom.domain.board.repository.board;

import kr.co.dearbloom.domain.board.entity.board.PickBoardHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PickBoardHistoryRepository extends JpaRepository<PickBoardHistory, Long> {
}
