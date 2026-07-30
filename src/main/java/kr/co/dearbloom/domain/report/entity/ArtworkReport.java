package kr.co.dearbloom.domain.report.entity;

import jakarta.persistence.*;
import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.domain.customer.entity.Customer;
import kr.co.dearbloom.global.entity.BaseTime;
import lombok.*;

/**
 * 고객이 작품을 신고한 기록. 신고자당 작품 1건(중복 신고 불가).
 * 신고 사유는 자유 텍스트로 받고, 처리(반려·블라인드)는 어드민 API 에서 다룬다.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
@Table(uniqueConstraints = @UniqueConstraint(
        name = "uk_artwork_report_customer_artwork",
        columnNames = {"customer_id", "artwork_id"}))
public class ArtworkReport extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long artworkReportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artwork_id", nullable = false)
    private Artwork artwork;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
}
