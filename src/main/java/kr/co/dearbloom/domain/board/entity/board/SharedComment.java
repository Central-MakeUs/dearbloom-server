package kr.co.dearbloom.domain.board.entity.board;

import jakarta.persistence.*;
import kr.co.dearbloom.domain.customer.entity.Customer;
import kr.co.dearbloom.global.entity.BaseTime;
import lombok.*;

/** 공동 보드에 남기는 댓글. 개별 공유 작품이 아니라 보드 단위로 달린다. */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
public class SharedComment extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sharedCommentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_board_id", nullable = false)
    private SharedBoard sharedBoard;

    // 댓글 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    public boolean isWrittenBy(Customer customer) {
        return this.customer.getCustomerId().equals(customer.getCustomerId());
    }
}
