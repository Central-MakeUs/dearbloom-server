package kr.co.dearbloom.domain.board.repository.board;

import kr.co.dearbloom.domain.board.entity.board.SharedBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SharedBoardRepository extends JpaRepository<SharedBoard, Long> {
    boolean existsByInviteCode(String inviteCode);

    // 초대 화면은 비로그인도 열 수 있어 방장 이름을 함께 내려줘야 한다 — 고객까지 fetch join.
    @Query("select b from SharedBoard b join fetch b.owner where b.inviteCode = :inviteCode")
    Optional<SharedBoard> findByInviteCodeWithOwner(@Param("inviteCode") String inviteCode);
}
