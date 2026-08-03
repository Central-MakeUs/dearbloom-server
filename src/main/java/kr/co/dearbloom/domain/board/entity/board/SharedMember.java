package kr.co.dearbloom.domain.board.entity.board;

import jakarta.persistence.*;
import kr.co.dearbloom.domain.customer.entity.Customer;
import lombok.*;

/** 공동 보드 참여자. 방장도 생성 시 한 행으로 들어간다. */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
@Table(uniqueConstraints = @UniqueConstraint(
        name = "uk_shared_member_board_customer",
        columnNames = {"shared_board_id", "customer_id"}))
public class SharedMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sharedMemberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_board_id", nullable = false)
    private SharedBoard sharedBoard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
}
