package kr.co.dearbloom.domain.artwork.repository;

import kr.co.dearbloom.domain.artwork.dto.response.ArtworkPageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Optional;

/**
 * 작품 탐색 <b>첫 화면</b> 응답 캐시. 키가 하나뿐이다.
 * <p>
 * 캐시 대상은 파라미터가 하나도 없는 요청(전체 작품 최신순 첫 페이지)뿐이다. 필터를 넣으면
 * 날짜 × 지역 × 인원 × 정렬 × 커서로 조합이 폭발해 적중률은 낮고 무효화 대상만 늘어난다.
 * 반면 첫 화면은 모든 방문자가 반드시 통과하는 유일한 쿼리다.
 * <p>
 * <b>담는 값에 isSaved 는 없다</b>(전부 null). 사람마다 다른 값이라 캐시에 담으면 남의 저장 상태가 나간다.
 * 조회 시점에 {@link ArtworkPageResponse#artworkList()} 각 항목에 덧씌운다.
 * 덕분에 로그인 사용자도 이 캐시를 탄다.
 * <p>
 * <b>TTL 은 신선도용이 아니다.</b> 무효화는 커밋 후 이벤트로 즉시 일어나므로 평상시 TTL 이 발동할 일이 없다.
 * 새 무효화 지점을 빠뜨린 코드가 들어왔을 때 최대 {@value #TTL_MINUTES} 분이면 스스로 낫도록 두는 바닥이다.
 * (첫 화면 쿼리에는 날짜 조건이 안 붙어 시계에 의존하지 않는다 — 시간이 흘러도 결과가 바뀌지 않는다.)
 * <p>
 * <b>Redis 장애는 조회를 막지 않는다.</b> 읽기 실패는 캐시 미스로, 쓰기·삭제 실패는 로그만 남기고 넘어간다.
 * 캐시는 없어도 되는 것이고, 목록 조회는 없으면 안 되는 것이다.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ArtworkExploreCacheRepository {
    // v1 = 응답 스키마 버전. ArtworkPageResponse/ArtworkSummaryResponse 필드가 바뀌면 올린다
    // (구버전 JSON 이 남아 있어도 역직렬화가 깨지지 않고 그냥 미스가 된다).
    private static final String KEY = "artwork:explore:v1:first-page";
    private static final int TTL_MINUTES = 10;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    /** 캐시된 첫 화면. 없거나 Redis 가 죽었으면 empty(= 미스로 처리해 DB 로 내려간다). */
    public Optional<ArtworkPageResponse> find() {
        try {
            String json = redisTemplate.opsForValue().get(KEY);
            return json == null
                    ? Optional.empty()
                    : Optional.of(objectMapper.readValue(json, ArtworkPageResponse.class));
        } catch (Exception e) {
            log.warn("[ArtworkExploreCache] 조회 실패 — DB 로 진행: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /** 첫 화면을 캐시에 올린다. 넘기는 응답의 isSaved 는 전부 null 이어야 한다. */
    public void save(ArtworkPageResponse page) {
        try {
            redisTemplate.opsForValue()
                    .set(KEY, objectMapper.writeValueAsString(page), Duration.ofMinutes(TTL_MINUTES));
        } catch (Exception e) {
            log.warn("[ArtworkExploreCache] 저장 실패 — 캐시 없이 진행: {}", e.getMessage());
        }
    }

    /** 캐시를 버린다. 멱등하다 — 같은 변경으로 여러 번 불려도 문제없다. */
    public void evict() {
        try {
            redisTemplate.delete(KEY);
        } catch (Exception e) {
            log.warn("[ArtworkExploreCache] 삭제 실패 — 최대 {}분 뒤 TTL 로 만료된다: {}", TTL_MINUTES, e.getMessage());
        }
    }
}
