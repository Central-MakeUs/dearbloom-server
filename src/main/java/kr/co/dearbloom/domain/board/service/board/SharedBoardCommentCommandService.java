package kr.co.dearbloom.domain.board.service.board;

import kr.co.dearbloom.domain.board.entity.board.SharedBoard;
import kr.co.dearbloom.domain.board.entity.board.SharedBoardComment;
import kr.co.dearbloom.domain.board.repository.board.SharedBoardCommentRepository;
import kr.co.dearbloom.domain.customer.entity.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SharedBoardCommentCommandService {
    private final SharedBoardCommentRepository sharedBoardCommentRepository;

    // 보드에 댓글 등록. 참여자 검증은 호출부(파사드) 책임.
    public SharedBoardComment create(SharedBoard sharedBoard, Customer customer, String content) {
        return sharedBoardCommentRepository.save(SharedBoardComment.builder()
                .sharedBoard(sharedBoard)
                .customer(customer)
                .content(content)
                .build());
    }

    // 댓글 삭제. 작성자 검증은 조회 시점(getWrittenBy)에 끝난 상태.
    public void delete(SharedBoardComment sharedBoardComment) {
        sharedBoardCommentRepository.delete(sharedBoardComment);
    }

    // 보드 삭제 시 그 보드의 댓글을 모두 정리(보드 삭제 경로에서 호출).
    public void deleteBySharedBoard(SharedBoard sharedBoard) {
        sharedBoardCommentRepository.deleteBySharedBoard(sharedBoard);
    }

    // 참여자 탈퇴 시 그가 이 보드에 남긴 댓글을 정리(탈퇴 경로에서 호출).
    public void deleteBySharedBoardAndCustomer(SharedBoard sharedBoard, Customer customer) {
        sharedBoardCommentRepository.deleteBySharedBoardAndCustomer(sharedBoard, customer);
    }
}
