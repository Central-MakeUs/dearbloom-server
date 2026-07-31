package kr.co.dearbloom.domain.report.entity;

import jakarta.persistence.*;
import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.domain.chat.entity.ChatMessage;
import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.domain.member.entity.MemberRole;
import kr.co.dearbloom.global.entity.BaseTime;
import lombok.*;

/**
 * 신고 1건. 대상 종류가 여러 개일 수 있어 한 테이블에서 처리한다 —
 * {@code targetType} 이 어느 대상인지 가리키고, 대상별 FK 컬럼(artwork 등) 중 <b>하나만</b> 채운다.
 * FK 를 타입별로 두는 이유는 참조 무결성과 JOIN 을 살리기 위함(단일 target_id 다형성 컬럼이면 둘 다 잃는다).
 * <p>
 * 반면 <b>신고자는 {@link Member} 하나로 합친다.</b> 고객·작가는 같은 Member 에 달린 프로필이라 신원이 하나이고,
 * 역할은 "어떤 모자를 쓰고 신고했는지"에 불과하기 때문. 신고자에도 역할별 FK 를 두면 중복 방지 unique 가
 * (신고자 종류 × 대상 종류)로 곱해져 불어난다.
 * <p>
 * 신고 방향은 고객→작가 / 작가→고객 만 허용한다. 이는 스키마가 아니라 역할별 엔드포인트 분리와
 * "자기 것은 신고 불가" 검증(파사드)으로 지킨다. 그래서 {@code reporterRole} 은 대상으로부터 유도 가능하지만
 * (ARTWORK=고객, CHAT_MESSAGE=발신자의 반대), 어드민 조회를 단순하게 하려고 스냅샷으로 저장한다.
 * <p>
 * 대상을 추가할 때: {@link ReportTargetType} 에 값 추가 → 여기에 nullable {@code @ManyToOne} 필드와
 * 정적 팩토리 추가 → {@code (member_id, 새 FK)} unique 제약 추가.
 * "정확히 하나만 채워짐"은 정적 팩토리로만 생성 경로를 열어 보장한다(ddl-auto:update 라 CHECK 제약은 기존 테이블에 반영되지 않음).
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
@Entity
@Table(uniqueConstraints = {
        // 대상별로 따로 건다 — MySQL 은 NULL 끼리 중복을 허용하므로 다른 타입 행끼리 간섭하지 않는다.
        // 역할을 넣지 않아 한 사람이 역할을 바꿔가며 같은 대상을 다시 신고할 수 없다.
        @UniqueConstraint(name = "uk_report_member_artwork",
                columnNames = {"member_id", "artwork_id"}),
        @UniqueConstraint(name = "uk_report_member_chat_message",
                columnNames = {"member_id", "chat_message_id"})})
public class Report extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    // 신고자(고객·작가 공통). 프로필이 아니라 Member 로 잡는다 — 위 클래스 주석 참고.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member reporter;

    // 어떤 역할로 신고했는지(스냅샷).
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole reporterRole;

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

    /** 작품 신고 생성. 작품 주인이 작가이므로 신고자는 항상 고객이다. */
    public static Report ofArtwork(Member reporter, Artwork artwork, String content) {
        return Report.builder()
                .reporter(reporter)
                .reporterRole(MemberRole.CUSTOMER)
                .targetType(ReportTargetType.ARTWORK)
                .artwork(artwork)
                .content(content)
                .build();
    }

    /** 채팅 메시지 신고 생성. 신고자 역할은 발신자의 반대편(자기 메시지는 신고할 수 없다 — 검증은 파사드). */
    public static Report ofChatMessage(Member reporter, ChatMessage chatMessage, String content) {
        return Report.builder()
                .reporter(reporter)
                .reporterRole(opposite(chatMessage.getSenderRole()))
                .targetType(ReportTargetType.CHAT_MESSAGE)
                .chatMessage(chatMessage)
                .content(content)
                .build();
    }

    private static MemberRole opposite(MemberRole role) {
        return role == MemberRole.CUSTOMER ? MemberRole.ARTIST : MemberRole.CUSTOMER;
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
