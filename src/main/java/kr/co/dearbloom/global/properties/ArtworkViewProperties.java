package kr.co.dearbloom.global.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 작품 조회수 집계 설정.
 *
 * @param dedupTtl 같은 회원의 같은 작품 재조회를 무시하는 기간. 이 시간 안의 재조회는 세지 않는다
 */
@ConfigurationProperties(prefix = "artwork.view")
public record ArtworkViewProperties(Duration dedupTtl) {
    public Duration dedupTtlOrDefault() {
        return dedupTtl != null ? dedupTtl : Duration.ofHours(1);
    }
}
