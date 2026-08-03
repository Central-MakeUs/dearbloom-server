package kr.co.dearbloom.domain.board.entity.artwork;

import jakarta.persistence.*;
import kr.co.dearbloom.domain.customer.entity.Customer;
import lombok.*;

/** 공유 작품 좋아요. 한 참여자가 같은 작품에 한 번만 누를 수 있다. */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
@Table(uniqueConstraints = @UniqueConstraint(
        name = "uk_shared_artwork_like_artwork_customer",
        columnNames = {"shared_artwork_id", "customer_id"}))
public class SharedArtworkLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sharedArtworkLikeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_artwork_id", nullable = false)
    private SharedArtwork sharedArtwork;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
}
