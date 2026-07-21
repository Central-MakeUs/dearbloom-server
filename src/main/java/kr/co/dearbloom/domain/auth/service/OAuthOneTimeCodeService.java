package kr.co.dearbloom.domain.auth.service;

import kr.co.dearbloom.global.dto.response.exception.CustomException;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

/**
 * 로컬 웹 ↔ 개발 서버 하이브리드 로그인용 one-time code 발급/교환.
 *
 * <p>로컬 프론트(localhost)와 개발 백엔드(dev-api.dearbloom.co.kr)는 서로 다른 사이트라
 * 백엔드의 {@code Set-Cookie} 가 로컬 브라우저에 저장되지 않는다. 그래서 로그인 성사 시
 * 토큰을 직접 심는 대신, 짧은 TTL 의 1회용 code 만 URL 로 넘기고(원문 JWT 미노출),
 * 로컬 Next.js 서버가 이 code 를 토큰으로 교환한 뒤 자기 도메인 쿠키를 심는다.
 *
 * <p>code 는 opaque(랜덤 UUID) 이며 Redis 에 {@code memberId} 만 매핑한다. 30초 TTL 에
 * consume 시 즉시 삭제(getAndDelete)해 재사용을 막는다.
 */
@Service
@RequiredArgsConstructor
public class OAuthOneTimeCodeService {
    private static final String KEY_PREFIX = "oauth:otc:";
    private static final Duration TTL = Duration.ofSeconds(30);

    private final StringRedisTemplate redis;

    /** memberId 를 담은 1회용 code 발급. */
    public String issue(Long memberId) {
        String code = UUID.randomUUID().toString().replace("-", "");
        redis.opsForValue().set(KEY_PREFIX + code, String.valueOf(memberId), TTL);
        return code;
    }

    /** code 를 memberId 로 교환하고 즉시 폐기. 없거나 만료면 예외. */
    public Long consume(String oneTimeCode) {
        String memberId = redis.opsForValue().getAndDelete(KEY_PREFIX + oneTimeCode);
        if (memberId == null) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        return Long.valueOf(memberId);
    }
}
