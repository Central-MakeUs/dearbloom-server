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

    // 보드 참여자 전원(입장 순, 방장 포함). 고객 fetch join.
    public List<SharedMember> getMembers(SharedBoard sharedBoard) {
        return sharedMemberRepository.findBySharedBoardWithCustomer(sharedBoard);
    }

    // 이 고객의 참여자 행. 참여 중이 아니면 403(보드 내부 정보 접근·탈퇴 전 검증용).
    public SharedMember getJoinedMember(SharedBoard sharedBoard, Customer customer) {
        return sharedMemberRepository.findBySharedBoardAndCustomer(sharedBoard, customer)
                .orElseThrow(() -> new CustomException(ErrorCode.SHARED_MEMBER_NOT_JOINED));
    }
}
