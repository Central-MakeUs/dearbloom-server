package kr.co.dearbloom.domain.inquiry.entity;

import jakarta.persistence.*;
import kr.co.dearbloom.domain.artist.entity.timeslot.TimeSlot;
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

    // 변경 전 일정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "previous_time_slot_id")
    private TimeSlot previousTimeSlot;

    // 재안된 일정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposed_time_slot_id")
    private TimeSlot proposedTimeSlot;

    // 제안자
    private String proposedBy;

    // 협의 상태
    @Enumerated(EnumType.STRING)
    private NegotiationStatus negotiationStatus;
}
