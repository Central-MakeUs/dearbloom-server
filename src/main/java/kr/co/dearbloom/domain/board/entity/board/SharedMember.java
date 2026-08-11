package kr.co.dearbloom.domain.board.entity.board;

import jakarta.persistence.*;
import kr.co.dearbloom.domain.customer.entity.Customer;
import kr.co.dearbloom.global.entity.BaseTime;
import lombok.*;

import java.time.LocalDateTime;

/** 공동 보드 참여자. 방장도 생성 시 한 행으로 들어간다. */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
@Table(uniqueConstraints = @UniqueConstraint(
        name = "uk_shared_member_board_customer",
        columnNames = {"shared_board_id", "customer_id"}))
public class SharedMember extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sharedMemberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_board_id", nullable = false)
    private SharedBoard sharedBoard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    /**
     * 이 보드의 댓글을 어디까지 읽었는지. 참여자가 N명이라 사람마다 따로 관리해야 해서
     * (보드 × 참여자) 행인 여기에 둔다. null 이면 아직 한 번도 읽지 않은 것.
     */
    private LocalDateTime lastReadCommentAt;

    /** 댓글 읽음 처리. 이 시각 이전에 달린 댓글은 모두 읽은 것으로 본다. */
    public void markCommentsRead(LocalDateTime at) {
        this.lastReadCommentAt = at;
    }
}
