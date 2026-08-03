package kr.co.dearbloom.domain.board.entity.artwork;

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
public class SharedArtworkComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sharedArtworkCommentId;

    // 어느 보드의 어느 작품에 단 코멘트인지 — 보드는 sharedArtwork 를 통해 따라간다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_artwork_id", nullable = false)
    private SharedArtwork sharedArtwork;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @CreatedDate
    private LocalDateTime createdAt;
}
