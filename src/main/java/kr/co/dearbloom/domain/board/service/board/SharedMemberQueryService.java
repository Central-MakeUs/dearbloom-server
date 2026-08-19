package kr.co.dearbloom.domain.board.service.board;

import kr.co.dearbloom.domain.board.entity.board.SharedBoard;
import kr.co.dearbloom.domain.board.entity.board.SharedMember;
import kr.co.dearbloom.domain.board.repository.board.SharedMemberRepository;
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
public class SharedMemberQueryService {
    private final SharedMemberRepository sharedMemberRepository;

    // 내가 참여 중인 보드 목록(보드 생성 오름차순).
    public List<SharedBoard> getJoinedBoards(Customer customer) {
        return sharedMemberRepository.findBoardsByCustomerOrderByCreatedAtAsc(customer);
    }

    // 보드 참여자 전원(입장 순). 고객 fetch join.
    public List<SharedMember> getMembers(SharedBoard sharedBoard) {
        return sharedMemberRepository.findBySharedBoardWithCustomer(sharedBoard);
    }

    // 참여 인원. 초대 화면에서 목록 없이 인원만 필요할 때.
    public long countMembers(SharedBoard sharedBoard) {
        return sharedMemberRepository.countBySharedBoard(sharedBoard);
    }

    // 이 고객이 참여 중인지. 초대 화면은 비로그인도 오므로 customerId(null 가능)로 판정한다.
    public boolean isJoined(SharedBoard sharedBoard, Long customerId) {
        return customerId != null
                && sharedMemberRepository.existsBySharedBoardAndCustomer_CustomerId(sharedBoard, customerId);
    }

    /** 작성자를 뺀 참여자들의 memberId. 보드 알림을 "나 말고 나머지" 에게 보낼 때 쓴다. */
    public List<Long> getOtherMemberIds(Long sharedBoardId, Long excludeCustomerId) {
        return sharedMemberRepository
                .findMemberIdsBySharedBoardIdExcludingCustomer(sharedBoardId, excludeCustomerId);
    }

    // 이 고객의 참여자 행. 참여 중이 아니면 403(보드 내부 정보 접근·탈퇴 전 검증용).
    public SharedMember getJoinedMember(SharedBoard sharedBoard, Customer customer) {
        return sharedMemberRepository.findBySharedBoardAndCustomer(sharedBoard, customer)
                .orElseThrow(() -> new CustomException(ErrorCode.SHARED_MEMBER_NOT_JOINED));
    }
}
