package kr.co.dearbloom.domain.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.global.entity.BaseTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 푸시 발송 대상 기기 토큰. 한 회원이 기기를 여러 대 쓸 수 있어 1:N 이다.
 *
 * <p>토큰 문자열에 unique 를 건다. 기기 하나를 두 사람이 번갈아 로그인하면 같은 토큰이 다시 올라오는데,
 * 이때 행을 새로 만들면 이전 소유자에게 남의 알림이 간다. 그래서 새로 만들지 않고 {@link #transferTo} 로
 * 소유자를 옮긴다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "device_token",
        uniqueConstraints = @UniqueConstraint(name = "uk_device_token_token", columnNames = "token"))
public class DeviceToken extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long deviceTokenId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /** FCM registration token. 길이가 정해져 있지 않아 넉넉히 잡는다. */
    @Column(nullable = false, length = 512)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private DevicePlatform platform;

    /** 마지막으로 등록·갱신된 시각. 오래된 토큰을 정리할 때 기준으로 쓸 수 있다. */
    private LocalDateTime lastUsedAt;

    @Builder
    private DeviceToken(Member member, String token, DevicePlatform platform) {
        this.member = member;
        this.token = token;
        this.platform = platform;
        this.lastUsedAt = LocalDateTime.now();
    }

    /** 같은 기기가 다른 계정으로 재등록됐을 때 소유자를 옮긴다. */
    public void transferTo(Member newOwner, DevicePlatform newPlatform) {
        this.member = newOwner;
        this.platform = newPlatform;
        touch();
    }

    public void touch() {
        this.lastUsedAt = LocalDateTime.now();
    }
}
