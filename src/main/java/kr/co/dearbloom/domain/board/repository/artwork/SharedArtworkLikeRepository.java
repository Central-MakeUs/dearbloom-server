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

    /**
     * 이 보드의 작품별 좋아요 수. 같은 작품을 여러 참여자가 담아 행이 여러 개여도
     * 작품 단위로 합산한다(조회 화면이 작품 하나로 합쳐 보여주므로).
     */
    @Query("select sa.artwork.artworkId as artworkId, count(l) as likeCount from SharedArtworkLike l"
            + " join l.sharedArtwork sa where sa.sharedBoard = :sharedBoard group by sa.artwork.artworkId")
    List<ArtworkLikeCount> countGroupedByArtwork(@Param("sharedBoard") SharedBoard sharedBoard);

    // 이 보드에서 내가 좋아요한 작품 id 집합(리스트에서 좋아요 여부 일괄 판정용).
    @Query("select distinct sa.artwork.artworkId from SharedArtworkLike l join l.sharedArtwork sa"
            + " where sa.sharedBoard = :sharedBoard and l.customer = :customer")
    Set<Long> findLikedArtworkIds(@Param("sharedBoard") SharedBoard sharedBoard,
                                  @Param("customer") Customer customer);

    void deleteBySharedArtworkAndCustomer(SharedArtwork sharedArtwork, Customer customer);

    // 보드 삭제 시 그 보드 작품들에 달린 좋아요를 함께 정리(FK 제약 위반 방지).
    void deleteBySharedArtwork_SharedBoard(SharedBoard sharedBoard);

    // 참여자 탈퇴 시 그가 이 보드에서 누른 좋아요를 정리(남의 작품에 누른 것 포함).
    void deleteBySharedArtwork_SharedBoardAndCustomer(SharedBoard sharedBoard, Customer customer);

    // 공유 작품이 사라질 때 거기 달린 모든 좋아요를 함께 정리(누른 사람 무관).
    void deleteBySharedArtworkIn(Collection<SharedArtwork> sharedArtworks);

    /** 작품별 좋아요 수 집계 결과. */
    interface ArtworkLikeCount {
        Long getArtworkId();

        long getLikeCount();
    }
}
