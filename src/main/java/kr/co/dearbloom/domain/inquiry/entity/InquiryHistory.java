package kr.co.dearbloom.domain.inquiry.entity;

import jakarta.persistence.*;
import kr.co.dearbloom.domain.customer.entity.Customer;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
public class InquiryHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long inquiryHistoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiry_id", nullable = false)
    private Inquiry inquiry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    // 변경 전/제안 일정은 예약 도메인 구현 시 확정. 지금은 슬롯 참조를 두지 않는다.

    // 제안자
    private String proposedBy;

    // 협의 상태
    @Enumerated(EnumType.STRING)
    private NegotiationStatus negotiationStatus;
}
