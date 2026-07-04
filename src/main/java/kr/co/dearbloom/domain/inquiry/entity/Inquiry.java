package kr.co.dearbloom.domain.inquiry.entity;

import jakarta.persistence.*;
import kr.co.dearbloom.domain.artist.entity.Artist;
import kr.co.dearbloom.domain.artist.entity.TimeSlot;
import kr.co.dearbloom.domain.customer.entity.Customer;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
public class Inquiry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long inquiryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id", nullable = false)
    private Artist artist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_slot_id")
    private TimeSlot timeSlot;

    @Builder.Default
    private Boolean isSent = false;     // 전송 여부

    private LocalDateTime scheduledAt;  // 촬영 시간대

    private String location;            // 촬영 장소

    private Integer participantCount;   // 촬영 인원

    @Column(columnDefinition = "TEXT")
    private String requestNote;         // 요청 사항
}
