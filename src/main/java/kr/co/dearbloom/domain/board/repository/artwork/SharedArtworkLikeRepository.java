package kr.co.dearbloom.domain.board.repository.artwork;

import kr.co.dearbloom.domain.board.entity.artwork.SharedArtwork;
import kr.co.dearbloom.domain.board.entity.artwork.SharedArtworkLike;
import kr.co.dearbloom.domain.board.entity.board.SharedBoard;
import kr.co.dearbloom.domain.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Repository
public interface SharedArtworkLikeRepository extends JpaRepository<SharedArtworkLike, Long> {
    boolean existsBySharedArtworkAndCustomer(SharedArtwork sharedArtwork, Customer customer);

    /** 이 보드의 공유작품별 좋아요 수(정렬용). 같은 작품이 중복으로 담기지 않으므로 작품 단위로 합산할 필요가 없다. */
    @Query("select sa.sharedArtworkId as sharedArtworkId, count(l) as likeCount from SharedArtworkLike l"
            + " join l.sharedArtwork sa where sa.sharedBoard = :sharedBoard group by sa.sharedArtworkId")
    List<SharedArtworkLikeCount> countGroupedBySharedArtwork(@Param("sharedBoard") SharedBoard sharedBoard);

    // 이 보드에서 내가 좋아요한 공유작품 id 집합(리스트에서 좋아요 여부 일괄 판정용).
    @Query("select l.sharedArtwork.sharedArtworkId from SharedArtworkLike l"
            + " where l.sharedArtwork.sharedBoard = :sharedBoard and l.customer = :customer")
    Set<Long> findLikedSharedArtworkIds(@Param("sharedBoard") SharedBoard sharedBoard,
                                        @Param("customer") Customer customer);

    void deleteBySharedArtworkAndCustomer(SharedArtwork sharedArtwork, Customer customer);

    // 보드 삭제 시 그 보드 작품들에 달린 좋아요를 함께 정리(FK 제약 위반 방지).
    void deleteBySharedArtwork_SharedBoard(SharedBoard sharedBoard);

    // 참여자 탈퇴 시 그가 이 보드에서 누른 좋아요를 정리(남의 작품에 누른 것 포함).
    void deleteBySharedArtwork_SharedBoardAndCustomer(SharedBoard sharedBoard, Customer customer);

    // 공유 작품이 사라질 때 거기 달린 모든 좋아요를 함께 정리(누른 사람 무관).
    void deleteBySharedArtworkIn(Collection<SharedArtwork> sharedArtworks);

    /** 공유작품별 좋아요 수 집계 결과. */
    interface SharedArtworkLikeCount {
        Long getSharedArtworkId();

        long getLikeCount();
    }
}
