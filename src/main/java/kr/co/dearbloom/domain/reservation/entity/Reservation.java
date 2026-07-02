package kr.co.dearbloom.domain.reservation.entity;

import jakarta.persistence.*;
import kr.co.dearbloom.domain.artist.entity.timeslot.TimeSlot;
import kr.co.dearbloom.domain.artist.entity.work.Work;
import kr.co.dearbloom.domain.customer.entity.Customer;
import kr.co.dearbloom.domain.inquiry.entity.Inquiry;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiry_id", nullable = false)
    private Inquiry inquiry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_id", nullable = false)
    private Work work;

    // 한 슬롯당 예약 하나 (동시 예약 방지)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_slot_id", unique = true)
    private TimeSlot timeSlot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus reservationStatus;

    private LocalDateTime scheduledAt;      // 촬영 시간대

    private String location;                // 촬영 장소

    private Integer participantCount;       // 촬영 인원

    @Column(columnDefinition = "TEXT")
    private String requestNote;             // 요청 사항
}
