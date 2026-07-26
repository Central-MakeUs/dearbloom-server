package kr.co.dearbloom.domain.inquiry.entity;

import jakarta.persistence.*;
import kr.co.dearbloom.domain.artwork.entity.ArtworkPackage;
import kr.co.dearbloom.domain.customer.entity.Customer;
import kr.co.dearbloom.domain.university.entity.University;
import kr.co.dearbloom.global.dto.response.exception.CustomException;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import kr.co.dearbloom.global.entity.BaseTime;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 스마트 문의. 고객이 작품의 특정 패키지를 골라 촬영 일시·학교·인원·요청사항을 담아 작가에게 보낸다.
 * 문의 시점의 표시 정보(작가/작품/패키지명·가격)는 스냅샷으로 보존한다 — 이후 작가가 수정/삭제해도 문의 기록은 유지.
 * 슬롯 잠금은 하지 않는다(문의=제안). 실제 잠금은 예약 도메인에서.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
public class Inquiry extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long inquiryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artwork_package_id", nullable = false)
    private ArtworkPackage artworkPackage;

    // 문의 라이프사이클 4상태(단일 소스). 예약 완료/취소도 이 값으로 표현한다.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private InquiryStatus status = InquiryStatus.IN_PROGRESS;

    // 촬영 학교(장소). 대학 목록에서 고른 경우에만 FK 세팅(구조적 링크). 자유입력이면 null.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_id", nullable = true)
    private University university;

    // 학교명(표시/스냅샷). university 선택 시 그 이름, 자유입력 시 입력값. 항상 채워진다.
    @Column(nullable = false)
    private String schoolName;

    // ──────────────── 촬영 일시 ────────────────
    @Column(nullable = false)
    private LocalDate shootDate;             // 촬영 날짜

    @Column(nullable = false)
    private LocalTime startTime;             // 촬영 시작 시각(30분 경계)

    @Column(nullable = false)
    private Integer durationMinutesSnapshot; // 문의 당시 패키지 촬영 소요시간(분) → 종료시각 계산용

    @Column(nullable = false)
    private Integer headCount;               // 촬영 인원

    @Column(columnDefinition = "TEXT")
    private String requestNote;              // 요청 사항(선택)

    // ──────────────── 문의 당시 스냅샷 ────────────────
    // 작가·작품·패키지가 이후 수정/삭제되어도 문의 시점 값을 보존한다.
    @Column(nullable = false)
    private String artistNicknameSnapshot;   // 문의 당시 작가 닉네임

    @Column(nullable = false)
    private String artworkNameSnapshot;      // 문의 당시 작품명

    @Column(nullable = false)
    private String packageNameSnapshot;      // 문의 당시 패키지명

    private Integer priceSnapshot;           // 문의 당시 패키지 가격

    // ──────────────── 상태 전이 (state machine) ────────────────

    /** 문의 취소. 진행중일 때만 가능(고객/작가). */
    public void cancelAsInquiry() {
        requireStatus(InquiryStatus.IN_PROGRESS);
        this.status = InquiryStatus.INQUIRY_CANCELED;
    }

    /** 예약 완료. 진행중일 때만 가능(작가). 슬롯 잠금(=RESERVED 상태)·이력 기록은 호출부 책임. */
    public void reserve() {
        requireStatus(InquiryStatus.IN_PROGRESS);
        this.status = InquiryStatus.RESERVED;
    }

    /** 예약 취소. 예약 완료 상태에서만 가능(작가). 슬롯은 상태 변경으로 자동 해제된다. */
    public void cancelReservation() {
        requireStatus(InquiryStatus.RESERVED);
        this.status = InquiryStatus.RESERVATION_CANCELED;
    }

    private void requireStatus(InquiryStatus expected) {
        if (this.status != expected) {
            throw new CustomException(ErrorCode.INQUIRY_INVALID_STATUS);
        }
    }
}
