package kr.co.dearbloom.domain.board.entity.board;

import jakarta.persistence.*;
import kr.co.dearbloom.domain.customer.entity.Customer;
import kr.co.dearbloom.global.entity.BaseTime;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
public class SharedBoard extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sharedBoardId;

    // 방장 (보드 생성자)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private Customer owner;

    @Column(nullable = false)
    private String boardName;

    @Column(unique = true, nullable = false, length = 16)
    private String inviteCode;

    // 보드 이름 변경. 방장 검증은 호출부(서비스) 책임.
    public void updateBoardName(String boardName) {
        this.boardName = boardName;
    }

    public boolean isOwner(Customer customer) {
        return this.owner.getCustomerId().equals(customer.getCustomerId());
    }
}
