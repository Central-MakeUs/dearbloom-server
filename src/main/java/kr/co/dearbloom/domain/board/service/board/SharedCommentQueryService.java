package kr.co.dearbloom.domain.board.service.board;

import kr.co.dearbloom.domain.board.entity.board.SharedBoard;
import kr.co.dearbloom.domain.board.entity.board.SharedComment;
import kr.co.dearbloom.domain.board.entity.board.SharedMember;
import kr.co.dearbloom.domain.board.repository.board.SharedCommentRepository;
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
public class SharedCommentQueryService {
    private final SharedCommentRepository sharedCommentRepository;

    // 보드 댓글 전체(작성 순, 작성자 fetch join).
    public List<SharedComment> getBySharedBoard(SharedBoard sharedBoard) {
        return sharedCommentRepository.findBySharedBoardWithCustomer(sharedBoard);
    }

    // 이 참여자가 안 읽은 댓글 수(내가 쓴 댓글 제외). 보드 화면의 안읽음 뱃지용.
    public long countUnread(SharedMember sharedMember) {
        return sharedCommentRepository.countUnread(
                sharedMember.getSharedBoard(),
                sharedMember.getCustomer(),
                sharedMember.getLastReadCommentAt());
    }

    // 댓글을 조회하되 이 고객이 작성자인지 검증한다(삭제용). 없으면 404, 남의 댓글이면 403.
    public SharedComment getWrittenBy(Long sharedCommentId, Customer customer) {
        SharedComment sharedComment = sharedCommentRepository.findById(sharedCommentId)
                .orElseThrow(() -> new CustomException(
                        ErrorCode.SHARED_COMMENT_NOT_FOUND, sharedCommentId));
        if (!sharedComment.isWrittenBy(customer)) {
            throw new CustomException(ErrorCode.SHARED_COMMENT_ACCESS_DENIED);
        }
        return sharedComment;
    }
}
