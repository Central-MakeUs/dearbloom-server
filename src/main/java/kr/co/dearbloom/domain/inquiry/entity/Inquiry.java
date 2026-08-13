package kr.co.dearbloom.domain.inquiry.entity;

import jakarta.persistence.*;
import kr.co.dearbloom.domain.artist.entity.artist.Artist;
import kr.co.dearbloom.domain.artwork.entity.Artwork;
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
 * 문의 시점의 표시 정보(작가/작품/패키지명·가격·소요시간·보정본 수)는 스냅샷으로 보존한다 —
 * 이후 작가가 수정/삭제해도 문의 기록은 유지.
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

    // 문의를 받은 작가. 작가 쪽 조회(문의함·예약 슬롯·캘린더)가 전부 이 컬럼만 본다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id", nullable = false)
    private Artist artist;

    // 문의 대상 작품. 작품 상세 이동 링크용. 작품이 삭제되면 null 이 된다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artwork_id")
    private Artwork artwork;

    // 고른 패키지. 작품 삭제·패키지 교체 시 null 이 된다. 표시값은 아래 스냅샷이 들고 있어 기록은 남는다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artwork_package_id")
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

    private Integer finalPhotoCountSnapshot; // 문의 당시 패키지 보정본 수(미정이면 null)

    // ──────────────── 삭제된 작품·교체된 패키지 대응 ────────────────

    /** 작품이 살아있으면 그 작품, 삭제됐으면 null. */
    public Artwork getArtworkOrNull() {
        return artwork;
    }

    /** 작품 상세 이동용 ID. 삭제된 작품이면 null 이라 프론트가 이동 링크를 숨긴다. */
    public Long getArtworkIdOrNull() {
        return artwork == null ? null : artwork.getArtworkId();
    }

    /**
     * 작품 삭제 시 작품·패키지 참조를 끊는다(작품 삭제 전 호출).
     * 표시값은 스냅샷에 남아 있어 문의 기록은 그대로 유지되고, artist 는 끊지 않으므로 작가 문의함에도 계속 보인다.
     */
    public void detachArtwork() {
        this.artwork = null;
        this.artworkPackage = null;
    }

    /**
     * 패키지 교체 시 패키지 참조만 끊는다(기존 패키지 행 삭제 전 호출).
     * 작품은 그대로라 상세 이동 링크가 살아 있고, 고객이 문의할 때 합의된 조건은 스냅샷에 그대로 남는다.
     */
    public void detachArtworkPackage() {
        this.artworkPackage = null;
    }

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
