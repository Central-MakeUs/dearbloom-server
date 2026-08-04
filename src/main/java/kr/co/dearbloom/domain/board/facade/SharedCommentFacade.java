package kr.co.dearbloom.domain.board.facade;

import kr.co.dearbloom.domain.board.dto.board.request.SharedCommentCreateRequest;
import kr.co.dearbloom.domain.board.dto.board.response.SharedCommentResponse;
import kr.co.dearbloom.domain.board.entity.board.SharedBoard;
import kr.co.dearbloom.domain.board.entity.board.SharedComment;
import kr.co.dearbloom.domain.board.service.board.SharedCommentCommandService;
import kr.co.dearbloom.domain.board.service.board.SharedCommentQueryService;
import kr.co.dearbloom.domain.board.service.board.SharedBoardQueryService;
import kr.co.dearbloom.domain.board.service.board.SharedMemberQueryService;
import kr.co.dearbloom.domain.customer.entity.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 공동보드 댓글 조회·등록·삭제. 댓글은 개별 공유작품이 아니라 보드 단위로 달린다. */
@RequiredArgsConstructor
@Component
public class SharedCommentFacade {
    private final SharedBoardQueryService sharedBoardQueryService;
    private final SharedMemberQueryService sharedMemberQueryService;
    private final SharedCommentCommandService sharedCommentCommandService;
    private final SharedCommentQueryService sharedCommentQueryService;

    /**
     * 보드 댓글 목록(작성 순). 보드 내부 정보이므로 <b>참여 중인 고객만</b> 조회할 수 있다(참여자가 아니면 403).
     */
    @Transactional(readOnly = true)
    public List<SharedCommentResponse> getComments(Customer customer, Long sharedBoardId) {
        SharedBoard sharedBoard = sharedBoardQueryService.getById(sharedBoardId);
        sharedMemberQueryService.getJoinedMember(sharedBoard, customer);
        return sharedCommentQueryService.getBySharedBoard(sharedBoard).stream()
                .map(SharedCommentResponse::from)
                .toList();
    }

    // 보드에 댓글 등록. 참여 중인 고객만 남길 수 있다(참여자가 아니면 403).
    @Transactional
    public void create(Customer customer, Long sharedBoardId, SharedCommentCreateRequest request) {
        SharedBoard sharedBoard = sharedBoardQueryService.getById(sharedBoardId);
        sharedMemberQueryService.getJoinedMember(sharedBoard, customer);
        sharedCommentCommandService.create(sharedBoard, customer, request.getContent());
    }

    // 댓글 삭제. 본인이 쓴 댓글만 지울 수 있다(남의 댓글이면 403).
    @Transactional
    public void delete(Customer customer, Long sharedCommentId) {
        SharedComment sharedComment =
                sharedCommentQueryService.getWrittenBy(sharedCommentId, customer);
        sharedCommentCommandService.delete(sharedComment);
    }
}
