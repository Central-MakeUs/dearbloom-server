package kr.co.dearbloom.domain.board.repository.artwork;

import kr.co.dearbloom.domain.board.entity.artwork.SharedArtwork;
import kr.co.dearbloom.domain.board.entity.board.SharedBoard;
import kr.co.dearbloom.domain.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface SharedArtworkRepository extends JpaRepository<SharedArtwork, Long> {
    // 여러 보드에 담긴 공유 작품을 담은 순서대로, 작품까지 fetch join 해 한 번에 조회(작품 N+1 제거).
    @Query("select sa from SharedArtwork sa join fetch sa.artwork where sa.sharedBoard in :sharedBoards"
            + " order by sa.sharedArtworkId asc")
    List<SharedArtwork> findBySharedBoardInWithArtwork(
            @Param("sharedBoards") Collection<SharedBoard> sharedBoards);

    // 이 보드에 이 참여자가 담은 공유 작품(참여자 탈퇴 시 정리 대상).
    List<SharedArtwork> findBySharedBoardAndCustomer(SharedBoard sharedBoard, Customer customer);

    // 보드 삭제 시 그 보드의 공유 작품을 함께 정리(FK 제약 위반 방지).
    void deleteBySharedBoard(SharedBoard sharedBoard);
}
