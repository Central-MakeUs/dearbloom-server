package kr.co.dearbloom.domain.board.entity.candidate;

import jakarta.persistence.*;
import kr.co.dearbloom.domain.artist.entity.work.Work;
import kr.co.dearbloom.domain.board.entity.board.PickBoard;
import kr.co.dearbloom.domain.customer.entity.Customer;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
@Table(uniqueConstraints = @UniqueConstraint(
        name = "uk_pick_candidate_board_work",
        columnNames = {"pick_board_id", "work_id"}))
public class PickCandidate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pickCandidateId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pick_board_id", nullable = false)
    private PickBoard pickBoard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_id", nullable = false)
    private Work work;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SelectedStatus selectedStatus;
}
