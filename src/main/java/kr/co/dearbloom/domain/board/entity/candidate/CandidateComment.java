package kr.co.dearbloom.domain.board.entity.candidate;

import jakarta.persistence.*;
import kr.co.dearbloom.domain.customer.entity.Customer;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
public class CandidateComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long candidateCommentId;

    // 어느 보드의 어느 작품에 단 코멘트인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pick_candidate_id", nullable = false)
    private PickCandidate pickCandidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @CreatedDate
    private LocalDateTime createdAt;
}
