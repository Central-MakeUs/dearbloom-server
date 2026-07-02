package kr.co.dearbloom.domain.board.entity.board;

import jakarta.persistence.*;
import kr.co.dearbloom.domain.board.entity.candidate.PickCandidate;
import kr.co.dearbloom.domain.customer.entity.Customer;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
public class PickBoardHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pickBoardHistoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pick_board_id", nullable = false)
    private PickBoard pickBoard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    private Customer actor; // 행위자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_candidate_id")
    private PickCandidate targetCandidate; // 대상 후보

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PickBoardActionType actionType;

    private String previousValue;

    private String newValue;
}
