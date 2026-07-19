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

    // 내 저장 작품을 저장 최신순으로, 작가까지 fetch join 해 한 번에 조회(작가 N+1 제거).
    // regions 는 컬렉션이라 fetch join 대신 Artist.regions 의 @BatchSize 로 묶는다.
    @Query("select a from SavedArtwork sa join sa.artwork a join fetch a.artist"
            + " where sa.customer = :customer order by sa.savedArtworkId desc")
    List<Artwork> findSavedArtworksWithArtist(@Param("customer") Customer customer);

    void deleteByCustomerAndArtwork_ArtworkId(Customer customer, Long artworkId);

    void deleteByCustomerAndArtwork_ArtworkIdIn(Customer customer, Collection<Long> artworkIds);
}
