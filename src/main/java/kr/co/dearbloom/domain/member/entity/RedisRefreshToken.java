package kr.co.dearbloom.domain.member.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RedisHash("refreshToken")
public class RedisRefreshToken {
    @Id
    private Long memberId;

    private String name;
    private String token;
    private String ip;
    private String deviceInfo;
    private Instant issuedAt;
    private Instant expiresAt;
    private Instant lastUsedAt;

    @TimeToLive
    private Long ttl;

    // 토큰 회전(rotation)은 아직 미사용 — 추후 적용 시 주석 해제.
//    public void rotate(String newToken, Instant expiresAt, long ttlSeconds) {
//        this.token = newToken;
//        this.expiresAt = expiresAt;
//        this.lastUsedAt = Instant.now();
//        this.ttl = ttlSeconds;
//    }
}
