package kr.co.dearbloom.domain.board.service.board;

import kr.co.dearbloom.domain.board.entity.board.SharedBoard;
import kr.co.dearbloom.domain.board.entity.board.SharedBoardComment;
import kr.co.dearbloom.domain.board.repository.board.SharedBoardCommentRepository;
import kr.co.dearbloom.domain.customer.entity.Customer;
import kr.co.dearbloom.global.dto.response.exception.CustomException;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SharedBoardCommentQueryService {
    private final SharedBoardCommentRepository sharedBoardCommentRepository;

    // 보드 댓글 전체(작성 순, 작성자 fetch join).
    public List<SharedBoardComment> getBySharedBoard(SharedBoard sharedBoard) {
        return sharedBoardCommentRepository.findBySharedBoardWithCustomer(sharedBoard);
    }

    // 댓글을 조회하되 이 고객이 작성자인지 검증한다(삭제용). 없으면 404, 남의 댓글이면 403.
    public SharedBoardComment getWrittenBy(Long sharedBoardCommentId, Customer customer) {
        SharedBoardComment sharedBoardComment = sharedBoardCommentRepository.findById(sharedBoardCommentId)
                .orElseThrow(() -> new CustomException(
                        ErrorCode.SHARED_BOARD_COMMENT_NOT_FOUND, sharedBoardCommentId));
        if (!sharedBoardComment.isWrittenBy(customer)) {
            throw new CustomException(ErrorCode.SHARED_BOARD_COMMENT_ACCESS_DENIED);
        }
        return sharedBoardComment;
    }
}
