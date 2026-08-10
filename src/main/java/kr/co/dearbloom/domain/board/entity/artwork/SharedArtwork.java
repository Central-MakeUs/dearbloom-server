package kr.co.dearbloom.domain.board.entity.artwork;

import jakarta.persistence.*;
import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.domain.board.entity.board.SharedBoard;
import kr.co.dearbloom.domain.customer.entity.Customer;
import kr.co.dearbloom.global.entity.BaseTime;
import lombok.*;

/**
 * 공동 보드에 담긴 작품.
 * <p>
 * <b>같은 작품은 보드에 한 번만 담긴다.</b> 먼저 담은 사람이 임자라, 다른 참여자는 그 작품을 또 담을 수 없다.
 * {@code customer} 가 담은 사람이고, 그 작품을 뺄 수 있는 것도 그 사람뿐이다.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
@Table(uniqueConstraints = @UniqueConstraint(
        name = "uk_shared_artwork_board_artwork",
        columnNames = {"shared_board_id", "artwork_id"}))
public class SharedArtwork extends BaseTime {
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
