package kr.co.dearbloom.domain.board.entity.board;

import jakarta.persistence.*;
import kr.co.dearbloom.domain.customer.entity.Customer;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
@Table(uniqueConstraints = @UniqueConstraint(
        name = "uk_board_participant_pick_board_customer",
        columnNames = {"pick_board_id", "customer_id"}))
public class BoardParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long boardParticipantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pick_board_id", nullable = false)
    private PickBoard pickBoard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
}
