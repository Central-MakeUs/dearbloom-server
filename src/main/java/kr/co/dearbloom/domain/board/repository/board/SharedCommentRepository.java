package kr.co.dearbloom.domain.board.repository.board;

import kr.co.dearbloom.domain.board.entity.board.SharedBoard;
import kr.co.dearbloom.domain.board.entity.board.SharedComment;
import kr.co.dearbloom.domain.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SharedCommentRepository extends JpaRepository<SharedComment, Long> {
    // 보드 댓글을 작성 순으로, 작성자까지 fetch join 해 한 번에 조회(이름 N+1 제거).
    @Query("select c from SharedComment c join fetch c.customer where c.sharedBoard = :sharedBoard"
            + " order by c.sharedCommentId asc")
    List<SharedComment> findBySharedBoardWithCustomer(@Param("sharedBoard") SharedBoard sharedBoard);

    /**
     * 이 고객이 아직 안 읽은 댓글 수.
     * - 내가 쓴 댓글은 제외한다(쓰자마자 내 뱃지에 잡히면 안 된다).
     * - lastReadCommentAt 이 null(한 번도 안 읽음)이면 남의 댓글 전부가 안읽음이다.
     */
    @Query("select count(c) from SharedComment c"
            + " where c.sharedBoard = :sharedBoard"
            + " and c.customer <> :customer"
            + " and (:lastReadCommentAt is null or c.createdAt > :lastReadCommentAt)")
    long countUnread(@Param("sharedBoard") SharedBoard sharedBoard,
                     @Param("customer") Customer customer,
                     @Param("lastReadCommentAt") LocalDateTime lastReadCommentAt);

    // 보드 삭제 시 그 보드의 댓글을 함께 정리(FK 제약 위반 방지).
    void deleteBySharedBoard(SharedBoard sharedBoard);

    // 참여자 탈퇴 시 그가 이 보드에 남긴 댓글을 정리.
    void deleteBySharedBoardAndCustomer(SharedBoard sharedBoard, Customer customer);
}
