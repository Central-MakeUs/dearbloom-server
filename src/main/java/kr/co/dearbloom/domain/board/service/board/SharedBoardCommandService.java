package kr.co.dearbloom.domain.board.service.board;

import kr.co.dearbloom.domain.board.entity.board.SharedBoard;
import kr.co.dearbloom.domain.board.repository.board.SharedBoardRepository;
import kr.co.dearbloom.domain.customer.entity.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SharedBoardCommandService {
    private final SharedBoardRepository sharedBoardRepository;

    // 공동보드 생성. 생성한 고객이 방장이 된다(참여자 행 생성은 SharedMemberCommandService 담당).
    public SharedBoard create(Customer owner, String boardName) {
        return sharedBoardRepository.save(SharedBoard.builder()
                .owner(owner)
                .boardName(boardName)
                .build());
    }

    // 보드 이름 변경. 방장 검증은 조회 시점(getOwnedBy)에 끝난 상태.
    public void updateBoardName(SharedBoard sharedBoard, String boardName) {
        sharedBoard.updateBoardName(boardName);
    }

    // 보드 행 삭제. 하위 데이터(공유작품·참여자 등) 정리는 호출부(파사드) 책임.
    public void delete(SharedBoard sharedBoard) {
        sharedBoardRepository.delete(sharedBoard);
    }
}
