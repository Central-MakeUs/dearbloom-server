package kr.co.dearbloom.domain.board.service.board;

import kr.co.dearbloom.domain.board.entity.board.SharedBoard;
import kr.co.dearbloom.domain.customer.entity.Customer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 신규 고객을 안내용 공동보드에 자동으로 합류시킨다.
 * <p>
 * 합류는 {@code shared_member} 행을 만드는 일이라 <b>고객 프로필이 있어야</b> 한다.
 * 소셜 로그인 가입 시점에는 {@code Member} 만 생기고 {@code Customer} 는 아직 없으므로,
 * <b>고객 온보딩이 끝나는 시점</b>에 호출한다.
 * <p>
 * 대상 보드가 없으면(환경마다 ID 가 다르거나 아직 안 만들었을 때) <b>아무 일도 하지 않는다</b> —
 * 안내용 보드가 없다고 온보딩이 실패하면 안 되기 때문이다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SharedBoardAutoJoinService {
    private final SharedBoardQueryService sharedBoardQueryService;
    private final SharedMemberCommandService sharedMemberCommandService;

    /** 자동 합류시킬 공동보드 ID. 환경마다 다를 수 있어 설정으로 뺐다(미지정 시 6번). */
    @Value("${board.welcome-shared-board-id:6}")
    private Long welcomeSharedBoardId;

    public void joinWelcomeBoard(Customer customer) {
        if (welcomeSharedBoardId == null) {
            return;
        }
        sharedBoardQueryService.findById(welcomeSharedBoardId).ifPresentOrElse(
                board -> join(board, customer),
                () -> log.info("[AutoJoin] 안내용 공동보드(id={}) 가 없어 합류를 건너뛴다.", welcomeSharedBoardId));
    }

    // 이미 참여 중이면 기존 행을 그대로 쓴다(멱등) — 재온보딩·중복 호출에도 안전하다.
    private void join(SharedBoard board, Customer customer) {
        sharedMemberCommandService.joinIfAbsent(board, customer);
        log.info("[AutoJoin] 공동보드(id={}) 에 고객(id={}) 자동 합류.",
                board.getSharedBoardId(), customer.getCustomerId());
    }
}
