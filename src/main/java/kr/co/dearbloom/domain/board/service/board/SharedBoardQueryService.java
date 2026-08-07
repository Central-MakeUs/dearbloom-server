package kr.co.dearbloom.domain.board.service.board;

import kr.co.dearbloom.domain.board.entity.board.SharedBoard;
import kr.co.dearbloom.domain.board.repository.board.SharedBoardRepository;
import kr.co.dearbloom.domain.customer.entity.Customer;
import kr.co.dearbloom.global.dto.response.exception.CustomException;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SharedBoardQueryService {
    private final SharedBoardRepository sharedBoardRepository;

    // 방장 여부와 무관하게 보드를 조회한다(입장 등). 없으면 404.
    public SharedBoard getById(Long sharedBoardId) {
        return sharedBoardRepository.findById(sharedBoardId)
                .orElseThrow(() -> new CustomException(ErrorCode.SHARED_BOARD_NOT_FOUND, sharedBoardId));
    }

    // 초대 코드로 보드를 조회한다(초대 화면·입장). 코드가 없거나 폐기됐으면 404.
    public SharedBoard getByInviteCode(String inviteCode) {
        return sharedBoardRepository.findByInviteCodeWithOwner(inviteCode)
                .orElseThrow(() -> new CustomException(ErrorCode.SHARED_BOARD_INVITE_CODE_INVALID));
    }

    // 보드를 조회하되 이 고객이 방장인지 검증한다(이름 수정·삭제용). 없으면 404, 방장이 아니면 403.
    public SharedBoard getOwnedBy(Long sharedBoardId, Customer customer) {
        SharedBoard sharedBoard = getById(sharedBoardId);
        if (!sharedBoard.isOwner(customer)) {
            throw new CustomException(ErrorCode.SHARED_BOARD_ACCESS_DENIED);
        }
        return sharedBoard;
    }
}
