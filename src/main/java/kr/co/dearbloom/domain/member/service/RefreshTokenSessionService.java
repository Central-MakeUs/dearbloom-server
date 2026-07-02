package kr.co.dearbloom.domain.member.service;

import kr.co.dearbloom.domain.member.entity.RedisRefreshToken;
import kr.co.dearbloom.domain.member.repository.RedisRefreshTokenRepository;
import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.global.auth.jwt.TokenProvider;
import kr.co.dearbloom.global.properties.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@RequiredArgsConstructor
@Service
public class RefreshTokenSessionService {
    private final RedisRefreshTokenRepository redisRefreshTokenRepository;
    private final JwtProperties jwtProperties;
    private final TokenProvider tokenProvider;

    public void save(Member member, String token, String ip, String deviceInfo) {
        Instant now = Instant.now();
        long ttlSeconds = jwtProperties.refreshTokenExpiry().toSeconds();
        redisRefreshTokenRepository.save(RedisRefreshToken.builder()
                .memberId(member.getMemberId())
                .name(member.getName())
                .token(token)
                .ip(ip)
                .deviceInfo(deviceInfo)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(ttlSeconds))
                .lastUsedAt(now)
                .ttl(ttlSeconds)
                // activeRole/availableRoles 는 실제 발급된 토큰(JWT)을 디코딩해서 그대로 저장 — 토큰 내용과 항상 일치하도록 함
                .activeRole(tokenProvider.getActiveRole(token))
                .availableRoles(tokenProvider.getAvailableRoles(token))
                .build());
    }

    // 토큰 회전(rotation)은 아직 미사용 — 추후 적용 시 주석 해제.
//    public void save(RedisRefreshToken entity) {
//        redisRefreshTokenRepository.save(entity);
//    }
//
//    public Optional<RedisRefreshToken> find(Long memberId) {
//        return redisRefreshTokenRepository.findById(memberId);
//    }

    public void delete(Long memberId) {
        redisRefreshTokenRepository.deleteById(memberId);
    }
}
