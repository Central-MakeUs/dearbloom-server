package kr.co.dearbloom.domain.board.service.board;

import kr.co.dearbloom.domain.board.entity.board.SharedBoard;
import kr.co.dearbloom.domain.customer.entity.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/** 안내용 공동보드 자동 합류 — 보드가 없으면 온보딩을 막지 않고 조용히 건너뛴다. */
@ExtendWith(MockitoExtension.class)
class SharedBoardAutoJoinServiceTest {
    private static final Long WELCOME_BOARD_ID = 6L;

    @Mock SharedBoardQueryService sharedBoardQueryService;
    @Mock SharedMemberCommandService sharedMemberCommandService;
    @InjectMocks SharedBoardAutoJoinService sharedBoardAutoJoinService;

    private final Customer customer = Customer.builder().build();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(sharedBoardAutoJoinService, "welcomeSharedBoardId", WELCOME_BOARD_ID);
    }

    @Test
    void 보드가_있으면_합류시킨다() {
        SharedBoard board = SharedBoard.builder().build();
        given(sharedBoardQueryService.findById(WELCOME_BOARD_ID)).willReturn(Optional.of(board));

        sharedBoardAutoJoinService.joinWelcomeBoard(customer);

        verify(sharedMemberCommandService).joinIfAbsent(board, customer);
    }

    @Test
    void 보드가_없으면_아무_일도_하지_않는다() {
        given(sharedBoardQueryService.findById(WELCOME_BOARD_ID)).willReturn(Optional.empty());

        sharedBoardAutoJoinService.joinWelcomeBoard(customer);

        verifyNoInteractions(sharedMemberCommandService); // 예외도 던지지 않는다
    }

    @Test
    void 보드_ID_설정이_없으면_조회조차_하지_않는다() {
        ReflectionTestUtils.setField(sharedBoardAutoJoinService, "welcomeSharedBoardId", null);

        sharedBoardAutoJoinService.joinWelcomeBoard(customer);

        verifyNoInteractions(sharedBoardQueryService);
        verifyNoInteractions(sharedMemberCommandService);
    }
}
