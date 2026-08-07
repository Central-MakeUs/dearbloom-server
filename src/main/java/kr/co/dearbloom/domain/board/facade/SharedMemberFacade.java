package kr.co.dearbloom.domain.board.facade;

import kr.co.dearbloom.domain.board.dto.board.response.SharedBoardInviteCodeResponse;
import kr.co.dearbloom.domain.board.dto.board.response.SharedBoardInviteResponse;
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
import kr.co.dearbloom.global.auth.resolver.ViewerContext;
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

    /**
     * 초대 링크 진입 화면용 미리보기. <b>비로그인도 호출한다.</b>
     * 로그인 상태면 이미 참여 중인지까지 판정해, 프론트가 "참여하기" 대신 곧바로 보드로 보낼 수 있게 한다.
     * 보드 내부 정보는 담지 않는다 — 코드만 알면 누구나 볼 수 있는 응답이기 때문.
     */
    @Transactional(readOnly = true)
    public SharedBoardInviteResponse getInvite(ViewerContext viewer, String inviteCode) {
        SharedBoard sharedBoard = sharedBoardQueryService.getByInviteCode(inviteCode);
        // 작가로 접속 중이거나 비로그인이면 고객 식별이 안 되므로 미참여로 본다.
        Long customerId = viewer.isCustomer() ? viewer.activeProfileId() : null;
        return SharedBoardInviteResponse.of(
                sharedBoard,
                sharedMemberQueryService.countMembers(sharedBoard),
                sharedMemberQueryService.isJoined(sharedBoard, customerId));
    }

    /**
     * 초대 링크로 공동보드 입장. 코드가 유효하지 않으면 404.
     * <b>이미 참여 중이어도 성공</b>으로 응답한다 — 링크 재클릭이 에러 화면으로 끝나면 안 되기 때문.
     */
    @Transactional
    public SharedBoardJoinResponse joinByInviteCode(Customer customer, String inviteCode) {
        SharedBoard sharedBoard = sharedBoardQueryService.getByInviteCode(inviteCode);
        SharedMember sharedMember = sharedMemberCommandService.joinIfAbsent(sharedBoard, customer);
        return SharedBoardJoinResponse.from(sharedMember);
    }

    /**
     * 공유하기 화면용 초대 코드 조회. <b>참여 중인 멤버만</b> 받을 수 있다(아니면 403).
     * 코드를 보드 일반 응답에 싣지 않고 이 API 로만 내주는 이유는, 코드가 곧 입장 권한이라
     * 목록·삭제 응답을 타고 불필요하게 퍼지면 통제가 안 되기 때문이다.
     */
    @Transactional(readOnly = true)
    public SharedBoardInviteCodeResponse getInviteCode(Customer customer, Long sharedBoardId) {
        SharedBoard sharedBoard = sharedBoardQueryService.getById(sharedBoardId);
        sharedMemberQueryService.getJoinedMember(sharedBoard, customer);
        return SharedBoardInviteCodeResponse.from(sharedBoard);
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
