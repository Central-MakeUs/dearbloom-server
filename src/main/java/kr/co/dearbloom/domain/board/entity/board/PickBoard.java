package kr.co.dearbloom.domain.board.entity.board;

import jakarta.persistence.*;
import kr.co.dearbloom.domain.customer.entity.Customer;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
public class PickBoard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pickBoardId;

    // 방장 (보드 생성자)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private Customer owner;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String inviteCode;
}
