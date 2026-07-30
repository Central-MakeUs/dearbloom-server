package kr.co.dearbloom.domain.report.entity;

import jakarta.persistence.*;
import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.domain.chat.entity.ChatMessage;
import kr.co.dearbloom.domain.customer.entity.Customer;
import kr.co.dearbloom.global.entity.BaseTime;
import lombok.*;

/**
 * 신고 1건. 대상 종류가 여러 개일 수 있어 한 테이블에서 처리한다 —
 * {@code targetType} 이 어느 대상인지 가리키고, 대상별 FK 컬럼(artwork 등) 중 <b>하나만</b> 채운다.
 * FK 를 타입별로 두는 이유는 참조 무결성과 JOIN 을 살리기 위함(단일 target_id 다형성 컬럼이면 둘 다 잃는다).
 * <p>
 * 대상을 추가할 때: {@link ReportTargetType} 에 값 추가 → 여기에 nullable {@code @ManyToOne} 필드와
 * 정적 팩토리 추가 → {@code (customer_id, 새 FK)} unique 제약 추가.
 * "정확히 하나만 채워짐"은 정적 팩토리로만 생성 경로를 열어 보장한다(ddl-auto:update 라 CHECK 제약은 기존 테이블에 반영되지 않음).
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
@Entity
@Table(uniqueConstraints = {
        // 대상별로 따로 건다 — MySQL 은 NULL 끼리 중복을 허용하므로 다른 타입 행끼리 간섭하지 않는다.
        @UniqueConstraint(name = "uk_report_customer_artwork",
                columnNames = {"customer_id", "artwork_id"}),
        @UniqueConstraint(name = "uk_report_customer_chat_message",
                columnNames = {"customer_id", "chat_message_id"})})
public class Report extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    // 신고자. 현재는 고객만 신고할 수 있다(작가 신고가 필요해지면 reporter 축을 별도로 설계).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportTargetType targetType;

    // ──────────────── 대상별 FK (targetType 에 대응하는 하나만 채워진다) ────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artwork_id")
    private Artwork artwork;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_message_id")
    private ChatMessage chatMessage;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; // 신고 사유(자유 텍스트)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ReportStatus status = ReportStatus.RECEIVED;

    /** 작품 신고 생성. */
    public static Report ofArtwork(Customer customer, Artwork artwork, String content) {
        return Report.builder()
                .customer(customer)
                .targetType(ReportTargetType.ARTWORK)
                .artwork(artwork)
                .content(content)
                .build();
    }

    /** 채팅 메시지 신고 생성. */
    public static Report ofChatMessage(Customer customer, ChatMessage chatMessage, String content) {
        return Report.builder()
                .customer(customer)
                .targetType(ReportTargetType.CHAT_MESSAGE)
                .chatMessage(chatMessage)
                .content(content)
                .build();
    }

    /** 어드민 반려. */
    public void reject() {
        this.status = ReportStatus.REJECTED;
    }

    /** 어드민 처리완료. */
    public void resolve() {
        this.status = ReportStatus.RESOLVED;
    }
}
