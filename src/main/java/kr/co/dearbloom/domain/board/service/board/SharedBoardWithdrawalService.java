package kr.co.dearbloom.domain.board.service.board;

import kr.co.dearbloom.domain.board.entity.board.SharedBoard;
import kr.co.dearbloom.domain.board.entity.board.SharedMember;
import kr.co.dearbloom.domain.board.repository.board.SharedMemberRepository;
import kr.co.dearbloom.domain.board.service.artwork.SharedArtworkCommandService;
import kr.co.dearbloom.domain.customer.entity.Customer;
import kr.co.dearbloom.domain.customer.repository.CustomerRepository;
import kr.co.dearbloom.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 회원 탈퇴 시 그가 참여 중이던 공동보드를 정리한다.
 * <p>
 * 보드별로 남는 멤버 수에 따라 갈린다.
 * <ul>
 *   <li><b>본인 혼자</b> — 보드를 삭제한다.</li>
 *   <li><b>2명 이상</b> — 보드는 남긴다. 탈퇴자가 방장이면 남은 멤버 중 가장 먼저 입장한 사람에게 위임하고,
 *       탈퇴자가 남긴 댓글·좋아요·공유작품을 지운 뒤 멤버 행을 삭제한다.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SharedBoardWithdrawalService {
    private final SharedMemberRepository sharedMemberRepository;
    private final SharedMemberCommandService sharedMemberCommandService;
    private final SharedCommentCommandService sharedCommentCommandService;
    private final SharedArtworkCommandService sharedArtworkCommandService;
    private final SharedBoardCommandService sharedBoardCommandService;
    private final CustomerRepository customerRepository;

    public void cleanUpForWithdrawal(Member member) {
        customerRepository.findByMember(member).ifPresent(this::cleanUpBoards);
    }

    private void cleanUpBoards(Customer customer) {
        List<SharedBoard> joinedBoards = sharedMemberRepository.findBoardsByCustomerOrderByCreatedAtAsc(customer);
        for (SharedBoard sharedBoard : joinedBoards) {
            if (sharedMemberRepository.countBySharedBoard(sharedBoard) <= 1) {
                deleteBoard(sharedBoard);
            } else {
                leaveBoard(sharedBoard, customer);
            }
        }
    }

    // 마지막 한 명이 나가는 보드. 하위 데이터를 FK 역순으로 정리한다(댓글 → 좋아요·공유작품 → 멤버 → 보드).
    private void deleteBoard(SharedBoard sharedBoard) {
        sharedCommentCommandService.deleteBySharedBoard(sharedBoard);
        sharedArtworkCommandService.deleteBySharedBoard(sharedBoard);
        sharedMemberCommandService.deleteBySharedBoard(sharedBoard);
        sharedBoardCommandService.delete(sharedBoard);
    }

    // 다른 멤버가 남는 보드. 방장이면 먼저 위임한 뒤, 본인이 남긴 것만 지우고 빠진다.
    private void leaveBoard(SharedBoard sharedBoard, Customer customer) {
        if (sharedBoard.isOwner(customer)) {
            delegateOwner(sharedBoard, customer);
        }
        sharedCommentCommandService.deleteBySharedBoardAndCustomer(sharedBoard, customer);
        sharedArtworkCommandService.deleteBySharedBoardAndCustomer(sharedBoard, customer);
        sharedMemberRepository.findBySharedBoardAndCustomer(sharedBoard, customer)
                .ifPresent(sharedMemberCommandService::delete);
    }

    // 남은 멤버 중 가장 먼저 입장한 사람에게 방장을 넘긴다(입장 시각 오름차순).
    private void delegateOwner(SharedBoard sharedBoard, Customer leavingOwner) {
        sharedMemberRepository
                .findFirstBySharedBoardAndCustomerNotOrderByCreatedAtAscSharedMemberIdAsc(sharedBoard, leavingOwner)
                .map(SharedMember::getCustomer)
                .ifPresent(sharedBoard::delegateOwner);
    }
}
