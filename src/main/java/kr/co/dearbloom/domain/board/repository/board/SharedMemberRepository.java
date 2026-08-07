package kr.co.dearbloom.domain.board.repository.board;

import kr.co.dearbloom.domain.board.entity.board.SharedBoard;
import kr.co.dearbloom.domain.board.entity.board.SharedMember;
import kr.co.dearbloom.domain.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SharedMemberRepository extends JpaRepository<SharedMember, Long> {
    boolean existsBySharedBoardAndCustomer(SharedBoard sharedBoard, Customer customer);

    long countBySharedBoard(SharedBoard sharedBoard);

    // 초대 화면용. ViewerContext 는 Customer 엔티티가 아니라 profileId 만 주므로 id 로 판정한다.
    boolean existsBySharedBoardAndCustomer_CustomerId(SharedBoard sharedBoard, Long customerId);

    Optional<SharedMember> findBySharedBoardAndCustomer(SharedBoard sharedBoard, Customer customer);

    // 보드 참여자를 입장 순으로, 고객까지 fetch join 해 한 번에 조회(이름 N+1 제거).
    @Query("select sm from SharedMember sm join fetch sm.customer where sm.sharedBoard = :sharedBoard"
            + " order by sm.sharedMemberId asc")
    List<SharedMember> findBySharedBoardWithCustomer(@Param("sharedBoard") SharedBoard sharedBoard);

    // 내가 참여 중인 보드를 보드 생성 오름차순(먼저 만들어진 보드부터)으로 조회.
    @Query("select sm.sharedBoard from SharedMember sm where sm.customer = :customer"
            + " order by sm.sharedBoard.createdAt asc, sm.sharedBoard.sharedBoardId asc")
    List<SharedBoard> findBoardsByCustomerOrderByCreatedAtAsc(@Param("customer") Customer customer);

    // 보드 삭제 시 참여자 행을 함께 정리(FK 제약 위반 방지).
    void deleteBySharedBoard(SharedBoard sharedBoard);
}
