package kr.co.dearbloom.domain.reservation.entity;

import jakarta.persistence.*;
import kr.co.dearbloom.domain.artwork.entity.Artwork;
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
    @JoinColumn(name = "artwork_id", nullable = false)
    private Artwork artwork;

    // 동시 예약 방지용 예약 확정 셀(예약 1 : 셀 N, unique 제약)은 예약 도메인 구현 시 설계.

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus reservationStatus;

    private LocalDateTime scheduledAt;      // 촬영 시간대

    private String location;                // 촬영 장소

    private Integer participantCount;       // 촬영 인원

    @Column(columnDefinition = "TEXT")
    private String requestNote;             // 요청 사항

    // ──────────────── 문의 당시 스냅샷 ────────────────
    // 작가·작품이 이후 수정/삭제되어도 예약 시점 값을 보존한다.
    private String artistNicknameSnapshot;  // 문의 당시 작가 닉네임

    private String artworkNameSnapshot;     // 문의 당시 작품명

    private Integer priceSnapshot;          // 문의 당시 가격
}
