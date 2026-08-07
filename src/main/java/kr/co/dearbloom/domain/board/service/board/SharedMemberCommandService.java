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

@Service
@RequiredArgsConstructor
@Transactional
public class SharedMemberCommandService {
    private final SharedMemberRepository sharedMemberRepository;

    // 보드 참여자 행 생성(입장 / 보드 생성 시 방장 등록). 이미 참여 중이면 409.
    public SharedMember join(SharedBoard sharedBoard, Customer customer) {
        if (sharedMemberRepository.existsBySharedBoardAndCustomer(sharedBoard, customer)) {
            throw new CustomException(ErrorCode.SHARED_MEMBER_ALREADY_JOINED);
        }
        return sharedMemberRepository.save(SharedMember.builder()
                .sharedBoard(sharedBoard)
                .customer(customer)
                .build());
    }

    /**
     * 초대 링크로 입장. 이미 참여 중이면 기존 행을 그대로 돌려준다(멱등).
     * 링크는 카톡에 남아 재클릭이 흔하고, 그때 409 를 띄우면 정상 흐름이 에러 화면으로 끝난다.
     */
    public SharedMember joinIfAbsent(SharedBoard sharedBoard, Customer customer) {
        return sharedMemberRepository.findBySharedBoardAndCustomer(sharedBoard, customer)
                .orElseGet(() -> sharedMemberRepository.save(SharedMember.builder()
                        .sharedBoard(sharedBoard)
                        .customer(customer)
                        .build()));
    }

    // 참여자 행 삭제(탈퇴). 방장 여부·보드에 남긴 데이터 정리는 호출부(파사드) 책임.
    public void delete(SharedMember sharedMember) {
        sharedMemberRepository.delete(sharedMember);
    }

    // 보드 삭제 시 그 보드의 참여자를 모두 정리(보드 삭제 경로에서 호출).
    public void deleteBySharedBoard(SharedBoard sharedBoard) {
        sharedMemberRepository.deleteBySharedBoard(sharedBoard);
    }
}
