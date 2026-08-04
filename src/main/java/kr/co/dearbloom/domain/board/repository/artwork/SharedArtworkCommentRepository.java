package kr.co.dearbloom.domain.board.repository.artwork;

import kr.co.dearbloom.domain.board.entity.artwork.SharedArtwork;
import kr.co.dearbloom.domain.board.entity.artwork.SharedArtworkComment;
import kr.co.dearbloom.domain.board.entity.board.SharedBoard;
import kr.co.dearbloom.domain.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface SharedArtworkCommentRepository extends JpaRepository<SharedArtworkComment, Long> {
    // 보드 삭제 시 그 보드 작품들에 달린 댓글을 함께 정리(FK 제약 위반 방지).
    void deleteBySharedArtwork_SharedBoard(SharedBoard sharedBoard);

    // 참여자 탈퇴 시 그가 이 보드에 남긴 댓글을 정리(남의 작품에 단 것 포함).
    void deleteBySharedArtwork_SharedBoardAndCustomer(SharedBoard sharedBoard, Customer customer);

    // 공유 작품이 사라질 때 거기 달린 모든 댓글을 함께 정리(작성자 무관).
    void deleteBySharedArtworkIn(Collection<SharedArtwork> sharedArtworks);
}
