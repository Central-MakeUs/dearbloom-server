package kr.co.dearbloom.domain.customer.repository;

import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.domain.customer.entity.Customer;
import kr.co.dearbloom.domain.customer.entity.SavedArtwork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Repository
public interface SavedArtworkRepository extends JpaRepository<SavedArtwork, Long> {
    boolean existsByCustomerAndArtwork(Customer customer, Artwork artwork);

    boolean existsByCustomer_CustomerIdAndArtwork_ArtworkId(Long customerId, Long artworkId);

    // 이 고객이 저장한 작품 id 집합(리스트에서 저장 여부 일괄 판정용).
    @Query("select sa.artwork.artworkId from SavedArtwork sa where sa.customer.customerId = :customerId")
    Set<Long> findSavedArtworkIdsByCustomerId(@Param("customerId") Long customerId);

    // 위와 같지만 지금 페이지에 뜬 작품들로만 좁힌다(페이지네이션 목록에서 저장 여부 판정용).
    @Query("select sa.artwork.artworkId from SavedArtwork sa"
            + " where sa.customer.customerId = :customerId and sa.artwork.artworkId in :artworkIds")
    Set<Long> findSavedArtworkIdsByCustomerIdAndArtworkIdIn(@Param("customerId") Long customerId,
                                                            @Param("artworkIds") Collection<Long> artworkIds);

    // 내 저장 작품을 저장 최신순으로, 작가까지 fetch join 해 한 번에 조회(작가 N+1 제거).
    // regions 는 컬렉션이라 fetch join 대신 Artist.regions 의 @BatchSize 로 묶는다.
    @Query("select a from SavedArtwork sa join sa.artwork a join fetch a.artist"
            + " where sa.customer = :customer order by sa.savedArtworkId desc")
    List<Artwork> findSavedArtworksWithArtist(@Param("customer") Customer customer);

    // 작품 삭제 시 그 작품을 저장해둔 모든 고객의 행을 함께 정리(FK 제약 위반 방지).
    void deleteByArtwork(Artwork artwork);

    // 회원 탈퇴 시 이 고객의 저장 작품을 모두 정리
    void deleteByCustomer(Customer customer);

    void deleteByCustomerAndArtwork_ArtworkId(Customer customer, Long artworkId);

    void deleteByCustomerAndArtwork_ArtworkIdIn(Customer customer, Collection<Long> artworkIds);
}
