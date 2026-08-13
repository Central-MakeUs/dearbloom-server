package kr.co.dearbloom.domain.board.service.board;

import kr.co.dearbloom.domain.board.entity.board.SharedBoard;
import kr.co.dearbloom.domain.board.entity.board.SharedComment;
import kr.co.dearbloom.domain.board.repository.board.SharedCommentRepository;
import kr.co.dearbloom.domain.customer.entity.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SharedCommentCommandService {
    private final SharedCommentRepository sharedCommentRepository;

    // 보드에 댓글 등록. 참여자 검증은 호출부(파사드) 책임.
    public SharedComment create(SharedBoard sharedBoard, Customer customer, String content) {
        return sharedCommentRepository.save(SharedComment.builder()
                .sharedBoard(sharedBoard)
                .customer(customer)
                .content(content)
                .build());
    }

    // 댓글 삭제. 작성자 검증은 조회 시점(getWrittenBy)에 끝난 상태.
    public void delete(SharedComment sharedComment) {
        sharedCommentRepository.delete(sharedComment);
    }

    // 보드 삭제 시 그 보드의 댓글을 모두 정리(보드 삭제 경로에서 호출).
    public void deleteBySharedBoard(SharedBoard sharedBoard) {
        sharedCommentRepository.deleteBySharedBoard(sharedBoard);
    }

    // 참여자 탈퇴 시 그가 이 보드에 남긴 댓글을 정리(탈퇴 경로에서 호출).
    public void deleteBySharedBoardAndCustomer(SharedBoard sharedBoard, Customer customer) {
        sharedCommentRepository.deleteBySharedBoardAndCustomer(sharedBoard, customer);
    }
}
