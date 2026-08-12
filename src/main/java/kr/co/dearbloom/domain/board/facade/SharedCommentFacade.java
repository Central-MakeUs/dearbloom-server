package kr.co.dearbloom.domain.board.facade;

import kr.co.dearbloom.domain.board.dto.board.request.SharedCommentCreateRequest;
import kr.co.dearbloom.domain.board.dto.board.response.SharedCommentResponse;
import kr.co.dearbloom.domain.board.dto.board.response.SharedCommentUnreadCountResponse;
import kr.co.dearbloom.domain.board.entity.board.SharedBoard;
import kr.co.dearbloom.domain.board.entity.board.SharedComment;
import kr.co.dearbloom.domain.board.entity.board.SharedMember;
import kr.co.dearbloom.domain.board.service.board.SharedCommentCommandService;
import kr.co.dearbloom.domain.board.service.board.SharedCommentQueryService;
import kr.co.dearbloom.domain.board.service.board.SharedBoardQueryService;
import kr.co.dearbloom.domain.board.service.board.SharedMemberCommandService;
import kr.co.dearbloom.domain.board.service.board.SharedMemberQueryService;
import kr.co.dearbloom.domain.customer.entity.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Component
public class SharedCommentFacade {
    private final SharedBoardQueryService sharedBoardQueryService;
    private final SharedMemberQueryService sharedMemberQueryService;
    private final SharedMemberCommandService sharedMemberCommandService;
    private final SharedCommentCommandService sharedCommentCommandService;
    private final SharedCommentQueryService sharedCommentQueryService;

    /**
     * 보드 댓글 목록(작성 순). 보드 내부 정보이므로 <b>참여 중인 고객만</b> 조회할 수 있다(참여자가 아니면 403).
     * 삭제는 본인 댓글만 가능하므로 항목마다 isMine 을 채워 프론트가 삭제 버튼 노출을 판단하게 한다.
     */
    @Transactional(readOnly = true)
    public List<SharedCommentResponse> getComments(Customer customer, Long sharedBoardId) {
        SharedBoard sharedBoard = sharedBoardQueryService.getById(sharedBoardId);
        sharedMemberQueryService.getJoinedMember(sharedBoard, customer);
        return sharedCommentQueryService.getBySharedBoard(sharedBoard).stream()
                .map(sharedComment -> SharedCommentResponse.of(sharedComment, customer))
                .toList();
    }

    /**
     * 안읽은 댓글 수만 조회(뱃지 갱신용). 보드 화면 전체를 다시 받지 않아도 되도록 분리했다.
     * 참여 검증용으로 이미 읽는 참여자 행을 그대로 재사용한다(추가 조회 없음).
     */
    @Transactional(readOnly = true)
    public SharedCommentUnreadCountResponse getUnreadCount(Customer customer, Long sharedBoardId) {
        SharedBoard sharedBoard = sharedBoardQueryService.getById(sharedBoardId);
        SharedMember sharedMember = sharedMemberQueryService.getJoinedMember(sharedBoard, customer);
        return new SharedCommentUnreadCountResponse(sharedCommentQueryService.countUnread(sharedMember));
    }

    /**
     * 댓글 읽음 처리. 지금까지 달린 댓글을 모두 읽은 것으로 보고, 이후 안읽음 수는 0 이 된다.
     * 댓글 목록을 연 시점에 호출한다(참여자가 아니면 403).
     */
    @Transactional
    public void markCommentsRead(Customer customer, Long sharedBoardId) {
        SharedBoard sharedBoard = sharedBoardQueryService.getById(sharedBoardId);
        SharedMember sharedMember = sharedMemberQueryService.getJoinedMember(sharedBoard, customer);
        sharedMemberCommandService.markCommentsRead(sharedMember, LocalDateTime.now());
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
