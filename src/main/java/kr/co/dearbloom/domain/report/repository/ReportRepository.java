package kr.co.dearbloom.domain.report.repository;

import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.domain.customer.entity.Customer;
import kr.co.dearbloom.domain.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    boolean existsByCustomerAndArtwork(Customer customer, Artwork artwork);

    boolean existsByCustomer_CustomerIdAndArtwork_ArtworkId(Long customerId, Long artworkId);
}
