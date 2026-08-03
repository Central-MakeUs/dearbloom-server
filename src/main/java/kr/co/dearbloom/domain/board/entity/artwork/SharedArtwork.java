package kr.co.dearbloom.domain.board.entity.artwork;

import jakarta.persistence.*;
import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.domain.board.entity.board.SharedBoard;
import kr.co.dearbloom.domain.customer.entity.Customer;
import lombok.*;

/** 공동 보드에 올라온 작품. 같은 작품을 두 번 담을 수 없다. */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
@Table(uniqueConstraints = @UniqueConstraint(
        name = "uk_shared_artwork_board_artwork",
        columnNames = {"shared_board_id", "artwork_id"}))
public class SharedArtwork {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sharedArtworkId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_board_id", nullable = false)
    private SharedBoard sharedBoard;

    // 이 작품을 보드에 담은 참여자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artwork_id", nullable = false)
    private Artwork artwork;
}
