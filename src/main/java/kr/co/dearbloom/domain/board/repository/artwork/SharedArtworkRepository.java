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

    // 이 보드의 공유 작품을 담은 순으로, 작품·작가까지 fetch join 해 한 번에 조회(N+1 제거).
    // 같은 작품이 참여자 수만큼 여러 행으로 나올 수 있어 중복 제거는 조회 측(서비스)에서 한다.
    @Query("select sa from SharedArtwork sa join fetch sa.artwork a join fetch a.artist"
            + " where sa.sharedBoard = :sharedBoard order by sa.createdAt asc, sa.sharedArtworkId asc")
    List<SharedArtwork> findBySharedBoardWithArtwork(@Param("sharedBoard") SharedBoard sharedBoard);

    // 이 보드에 이 참여자가 담은 공유 작품(참여자 탈퇴·공유작품 업데이트 시 정리 대상).
    List<SharedArtwork> findBySharedBoardAndCustomer(SharedBoard sharedBoard, Customer customer);

    // 내 공유작품 목록을 작품까지 fetch join 해 조회(업데이트 응답용).
    @Query("select sa from SharedArtwork sa join fetch sa.artwork"
            + " where sa.sharedBoard = :sharedBoard and sa.customer = :customer"
            + " order by sa.sharedArtworkId asc")
    List<SharedArtwork> findBySharedBoardAndCustomerWithArtwork(@Param("sharedBoard") SharedBoard sharedBoard,
                                                                @Param("customer") Customer customer);

    // 보드 삭제 시 그 보드의 공유 작품을 함께 정리(FK 제약 위반 방지).
    void deleteBySharedBoard(SharedBoard sharedBoard);
}
