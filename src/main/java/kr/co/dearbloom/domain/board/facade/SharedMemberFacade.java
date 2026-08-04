package kr.co.dearbloom.domain.board.facade;

import kr.co.dearbloom.domain.board.dto.board.response.SharedBoardJoinResponse;
import kr.co.dearbloom.domain.board.dto.board.response.SharedMemberListResponse;
import kr.co.dearbloom.domain.board.dto.board.response.SharedMemberResponse;
import kr.co.dearbloom.domain.board.entity.board.SharedBoard;
import kr.co.dearbloom.domain.board.entity.board.SharedMember;
import kr.co.dearbloom.domain.board.service.artwork.SharedArtworkCommandService;
import kr.co.dearbloom.domain.board.service.board.SharedBoardQueryService;
import kr.co.dearbloom.domain.board.service.board.SharedCommentCommandService;
import kr.co.dearbloom.domain.board.service.board.SharedMemberCommandService;
import kr.co.dearbloom.domain.board.service.board.SharedMemberQueryService;
import kr.co.dearbloom.domain.customer.entity.Customer;
import kr.co.dearbloom.global.dto.response.exception.CustomException;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class SharedMemberFacade {
    private final SharedBoardQueryService sharedBoardQueryService;
    private final SharedMemberCommandService sharedMemberCommandService;
    private final SharedMemberQueryService sharedMemberQueryService;
    private final SharedCommentCommandService sharedCommentCommandService;
    private final SharedArtworkCommandService sharedArtworkCommandService;

    // 공동보드 입장. 이미 참여 중이면 409.
    @Transactional
    public SharedBoardJoinResponse join(Customer customer, Long sharedBoardId) {
        SharedBoard sharedBoard = sharedBoardQueryService.getById(sharedBoardId);
        SharedMember sharedMember = sharedMemberCommandService.join(sharedBoard, customer);
        return SharedBoardJoinResponse.from(sharedMember);
    }

    /**
     * 공동보드의 참여자 목록과 인원. 방장도 참여자 한 명으로 함께 나온다.
     * 보드 내부 정보이므로 <b>참여 중인 고객만</b> 조회할 수 있다(참여자가 아니면 403).
     */
    @Transactional(readOnly = true)
    public SharedMemberListResponse getMembers(Customer customer, Long sharedBoardId) {
        SharedBoard sharedBoard = sharedBoardQueryService.getById(sharedBoardId);
        sharedMemberQueryService.getJoinedMember(sharedBoard, customer);
        return SharedMemberListResponse.from(sharedMemberQueryService.getMembers(sharedBoard).stream()
                .map(SharedMemberResponse::from)
                .toList());
    }

    /**
     * 공동보드 탈퇴. 일반 참여자만 가능하며 <b>방장은 나갈 수 없다</b>(403, 보드 삭제만 가능).
     * 탈퇴자가 이 보드에 남긴 공유작품·좋아요·댓글을 함께 지우고 참여자 행을 삭제한다.
     */
    @Transactional
    public void leave(Customer customer, Long sharedBoardId) {
        SharedBoard sharedBoard = sharedBoardQueryService.getById(sharedBoardId);
        if (sharedBoard.isOwner(customer)) {
            throw new CustomException(ErrorCode.SHARED_BOARD_OWNER_CANNOT_LEAVE);
        }
        SharedMember sharedMember = sharedMemberQueryService.getJoinedMember(sharedBoard, customer);
        sharedCommentCommandService.deleteBySharedBoardAndCustomer(sharedBoard, customer);
        sharedArtworkCommandService.deleteBySharedBoardAndCustomer(sharedBoard, customer);
        sharedMemberCommandService.delete(sharedMember);
    }
}
