package kr.co.dearbloom.domain.chat.entity;

import jakarta.persistence.*;
import kr.co.dearbloom.domain.artist.entity.artist.Artist;
import kr.co.dearbloom.domain.customer.entity.Customer;
import kr.co.dearbloom.domain.member.entity.MemberRole;
import kr.co.dearbloom.global.entity.BaseTime;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 고객↔작가 1:1 채팅방. (고객, 작가) 쌍당 하나만 존재(unique). 문의 신청 시 자동 생성/재사용된다.
 * 목록 조회 성능을 위해 마지막 메시지 미리보기·시각과 안읽음 수를 방에 비정규화한다(나중에 Redis 로 이관 가능).
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(name = "uk_chat_room_pair", columnNames = {"customer_id", "artist_id"}),
        indexes = {
                @Index(name = "idx_chat_room_customer", columnList = "customer_id, last_message_at"),
                @Index(name = "idx_chat_room_artist", columnList = "artist_id, last_message_at")
        })
public class ChatRoom extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chatRoomId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id", nullable = false)
    private Artist artist;

    // 목록 표시용 비정규화 — 마지막 메시지 미리보기/시각.
    private String lastMessagePreview;

    private LocalDateTime lastMessageAt;

    // 안읽음 수(수신자 기준). 메시지 전송 시 수신자 쪽 +1, 읽음 처리 시 0.
    @Builder.Default
    @Column(nullable = false)
    private int customerUnread = 0;

    @Builder.Default
    @Column(nullable = false)
    private int artistUnread = 0;

    private LocalDateTime customerLastReadAt;

    private LocalDateTime artistLastReadAt;

    public static ChatRoom create(Customer customer, Artist artist) {
        return ChatRoom.builder()
                .customer(customer)
                .artist(artist)
                .build();
    }

    /** 새 메시지 반영. 미리보기·시각 갱신 + 수신자(발신자의 반대편) 안읽음 +1. */
    public void onNewMessage(String preview, LocalDateTime at, MemberRole senderRole) {
        this.lastMessagePreview = preview;
        this.lastMessageAt = at;
        if (senderRole == MemberRole.CUSTOMER) {
            this.artistUnread++;
        } else {
            this.customerUnread++;
        }
    }

    /** 읽음 처리. 해당 역할의 안읽음 0 + 마지막 읽은 시각 갱신. */
    public void markRead(MemberRole readerRole, LocalDateTime at) {
        if (readerRole == MemberRole.CUSTOMER) {
            this.customerUnread = 0;
            this.customerLastReadAt = at;
        } else {
            this.artistUnread = 0;
            this.artistLastReadAt = at;
        }
    }

    /** role·profileId 가 이 방의 참여자인지. */
    public boolean isParticipant(MemberRole role, Long profileId) {
        return role == MemberRole.CUSTOMER
                ? customer.getCustomerId().equals(profileId)
                : artist.getArtistId().equals(profileId);
    }

    public int unreadFor(MemberRole role) {
        return role == MemberRole.CUSTOMER ? customerUnread : artistUnread;
    }
}
