package kr.co.dearbloom.domain.inquiry.repository;

import kr.co.dearbloom.domain.inquiry.entity.Inquiry;
import kr.co.dearbloom.domain.inquiry.entity.InquiryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    // 이 고객이 고객으로 걸린, 특정 상태들의 문의(탈퇴·해지 시 자동 취소 대상 조회).
    @Query("""
            select i from Inquiry i
            where i.customer.customerId = :customerId
              and i.status in :statuses
            """)
    List<Inquiry> findByCustomerIdAndStatusIn(
            @Param("customerId") Long customerId,
            @Param("statuses") Collection<InquiryStatus> statuses);

    // 이 작가가 작가로 걸린, 특정 상태들의 문의(탈퇴·해지 시 자동 취소 대상 조회).
    @Query("""
            select i from Inquiry i
            where i.artworkPackage.artwork.artist.artistId = :artistId
              and i.status in :statuses
            """)
    List<Inquiry> findByArtistIdAndStatusIn(
            @Param("artistId") Long artistId,
            @Param("statuses") Collection<InquiryStatus> statuses);

    // 특정 작가의 특정 날짜, 특정 상태 문의(예약확정 슬롯 계산용).
    @Query("""
            select i from Inquiry i
            where i.artworkPackage.artwork.artist.artistId = :artistId
              and i.shootDate = :date
              and i.status = :status
            """)
    List<Inquiry> findByArtistAndShootDateAndStatus(
            @Param("artistId") Long artistId,
            @Param("date") LocalDate date,
            @Param("status") InquiryStatus status);

    // 특정 작가의 기간 내, 특정 상태 문의(캘린더 예약확정 슬롯 배치 계산용).
    @Query("""
            select i from Inquiry i
            where i.artworkPackage.artwork.artist.artistId = :artistId
              and i.shootDate between :from and :to
              and i.status = :status
            """)
    List<Inquiry> findByArtistAndShootDateBetweenAndStatus(
            @Param("artistId") Long artistId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("status") InquiryStatus status);

    // 고객 문의 리스트(최근 수정순). 대표 이미지용으로 작품까지 fetch join.
    @Query("""
            select i from Inquiry i
            join fetch i.artworkPackage p
            join fetch p.artwork
            where i.customer.customerId = :customerId
            order by i.modifiedAt desc
            """)
    List<Inquiry> findByCustomerOrderByModifiedAtDesc(@Param("customerId") Long customerId);

    // 작가 문의 리스트(내 작품에 들어온 문의, 촬영일 오름차순 → 같은 날은 시작시각 오름차순).
    @Query("""
            select i from Inquiry i
            where i.artworkPackage.artwork.artist.artistId = :artistId
            order by i.shootDate asc, i.startTime asc
            """)
    List<Inquiry> findByArtistOrderByShootDateAsc(@Param("artistId") Long artistId);
}
