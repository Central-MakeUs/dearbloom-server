package kr.co.dearbloom.domain.board.repository.board;

import kr.co.dearbloom.domain.board.entity.board.SharedBoard;
import kr.co.dearbloom.domain.board.entity.board.SharedBoardComment;
import kr.co.dearbloom.domain.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SharedBoardCommentRepository extends JpaRepository<SharedBoardComment, Long> {
    // 보드 댓글을 작성 순으로, 작성자까지 fetch join 해 한 번에 조회(이름 N+1 제거).
    @Query("select c from SharedBoardComment c join fetch c.customer where c.sharedBoard = :sharedBoard"
            + " order by c.sharedBoardCommentId asc")
    List<SharedBoardComment> findBySharedBoardWithCustomer(@Param("sharedBoard") SharedBoard sharedBoard);

    // 보드 삭제 시 그 보드의 댓글을 함께 정리(FK 제약 위반 방지).
    void deleteBySharedBoard(SharedBoard sharedBoard);

    // 참여자 탈퇴 시 그가 이 보드에 남긴 댓글을 정리.
    void deleteBySharedBoardAndCustomer(SharedBoard sharedBoard, Customer customer);
}
