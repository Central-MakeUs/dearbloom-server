package kr.co.dearbloom.domain.inquiry.entity;

import jakarta.persistence.*;
import kr.co.dearbloom.domain.member.entity.MemberRole;
import kr.co.dearbloom.global.entity.BaseTime;
import lombok.*;

/**
 * 문의 상태 변경 이력(감사 로그). 상태가 바뀔 때마다 1행씩 append-only 로 쌓는다.
 * createdAt(BaseTime) = 변경 시각. 시간순으로 읽으면 문의의 전체 타임라인이 재구성된다.
 * (예약 완료 시각은 toStatus=RESERVED 인 이력 행의 createdAt 으로 얻는다 — 별도 Reservation 불필요)
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
@Table(indexes = @Index(name = "idx_inquiry_history_inquiry", columnList = "inquiry_id, created_at"))
public class InquiryHistory extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long inquiryHistoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiry_id", nullable = false)
    private Inquiry inquiry;

    // 변경 전 상태. 최초 생성 이력이면 null.
    @Enumerated(EnumType.STRING)
    private InquiryStatus fromStatus;

    // 변경 후 상태.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InquiryStatus toStatus;

    // 변경 주체(고객/작가). 문의 1건엔 고객·작가 각 1명뿐이라 role 만으로 행위자가 특정된다.
    // 탈퇴·해지로 인한 시스템 자동 전이도 나가는 당사자의 role 로 기록하고, 사유는 reason 으로 구분한다.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole changedByRole;

    // 시스템 자동 전이 사유(회원 탈퇴/역할 해지 등). 일반 사용자 조작 전이는 null.
    @Column(columnDefinition = "TEXT")
    private String reason;
}
