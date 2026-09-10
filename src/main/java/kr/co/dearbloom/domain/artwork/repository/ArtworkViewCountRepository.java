package kr.co.dearbloom.domain.artwork.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;
import java.util.Set;

/**
 * 작품 조회수 집계용 Redis 저장소. 키 세 종류를 쓴다.
 *
 * <ul>
 *   <li>{@code artwork:view:dedup:{artworkId}:{memberId}} — 중복 제거용. TTL 이 붙는다</li>
 *   <li>{@code artwork:view:delta:{artworkId}} — 마지막 flush 이후 쌓인 <b>증가분</b></li>
 *   <li>{@code artwork:view:dirty} — 증가분이 있는 작품 ID 목록. flush 대상을 고르는 힌트</li>
 * </ul>
 *
 * <p><b>절대값이 아니라 증가분을 담는 이유.</b> Redis 를 진실로 두고 절대값을 DB 에 덮어쓰면,
 * Redis 가 재시작해 카운터가 0 부터 다시 시작할 때 DB 의 조회수가 <b>줄어든다</b>.
 * 증가분만 담고 DB 에 더하면 진실은 항상 DB 에 있고, 최악의 경우에도 한 주기치만 날아간다.
 *
 * <p><b>dirty 목록은 힌트일 뿐이다.</b> 전체 키를 훑는 {@code KEYS} 는 Redis 가 단일 스레드라
 * 도는 동안 서버 전체가 멈춘다. 그래서 조회가 일어난 작품만 따로 모아 둔다.
 * 이 목록이 유실돼도 증가분 자체는 남아 있어, 그 작품에 조회가 한 번 더 일어나면 함께 반영된다.
 *
 * <p><b>Redis 장애는 조회를 막지 않는다.</b> 조회수는 없어도 되는 것이고 작품 상세는 없으면 안 되는 것이라,
 * 모든 실패를 삼키고 로그만 남긴다.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ArtworkViewCountRepository {
    private static final String DEDUP_KEY = "artwork:view:dedup:%d:%d";
    private static final String DELTA_KEY = "artwork:view:delta:%d";
    private static final String DIRTY_KEY = "artwork:view:dirty";

    private final StringRedisTemplate redisTemplate;

    /**
     * 이번 조회를 셀 차례인지. 처음이면 true, TTL 안에 이미 봤으면 false.
     * <p>
     * {@code SET NX} 는 검사와 저장이 한 명령이라, 같은 사용자의 동시 요청에도 하나만 통과한다.
     * Redis 가 죽어 판단할 수 없으면 <b>세지 않는다</b> — 못 세는 쪽이 중복으로 부풀리는 쪽보다 낫다.
     */
    public boolean markFirstView(Long artworkId, Long memberId, Duration ttl) {
        try {
            return Boolean.TRUE.equals(redisTemplate.opsForValue()
                    .setIfAbsent(DEDUP_KEY.formatted(artworkId, memberId), "", ttl));
        } catch (Exception e) {
            log.warn("[ViewCount] 중복 검사 실패 — artworkId={}, {}", artworkId, e.getMessage());
            return false;
        }
    }

    /** 증가분 +1 과 dirty 등록. 조회 응답을 막으면 안 되므로 실패해도 넘어간다. */
    public void increase(Long artworkId) {
        try {
            redisTemplate.opsForValue().increment(DELTA_KEY.formatted(artworkId));
            redisTemplate.opsForSet().add(DIRTY_KEY, String.valueOf(artworkId));
        } catch (Exception e) {
            log.warn("[ViewCount] 증가 실패 — artworkId={}, {}", artworkId, e.getMessage());
        }
    }

    /** flush 대상 작품 ID. 비어 있으면 이번 주기에 할 일이 없다. */
    public List<Long> findDirtyArtworkIds() {
        Set<String> ids = redisTemplate.opsForSet().members(DIRTY_KEY);
        return ids == null ? List.of() : ids.stream().map(Long::valueOf).toList();
    }

    /** 지금까지 쌓인 증가분. 아직 지우지 않는다 — DB 반영이 성공해야 그만큼만 뺀다. */
    public long readDelta(Long artworkId) {
        String value = redisTemplate.opsForValue().get(DELTA_KEY.formatted(artworkId));
        return value == null ? 0L : Long.parseLong(value);
    }

    /**
     * DB 에 반영한 만큼만 증가분에서 뺀다. 남은 값이 0 이면 dirty 에서도 뺀다.
     * <p>
     * 통째로 지우지 않는 이유 — 읽은 뒤 빼기 전에 들어온 새 조회가 사라지면 안 된다.
     * 읽은 값만큼만 빼면 그 사이의 증가는 그대로 남아 다음 주기에 반영된다.
     */
    public void subtractApplied(Long artworkId, long applied) {
        Long remaining = redisTemplate.opsForValue().decrement(DELTA_KEY.formatted(artworkId), applied);
        if (remaining != null && remaining <= 0) {
            redisTemplate.delete(DELTA_KEY.formatted(artworkId));
            redisTemplate.opsForSet().remove(DIRTY_KEY, String.valueOf(artworkId));
        }
    }

    /** 작품이 사라졌을 때 남은 집계를 버린다(작품 삭제·flush 중 대상 없음). */
    public void clear(Long artworkId) {
        try {
            redisTemplate.delete(DELTA_KEY.formatted(artworkId));
            redisTemplate.opsForSet().remove(DIRTY_KEY, String.valueOf(artworkId));
        } catch (Exception e) {
            log.warn("[ViewCount] 집계 정리 실패 — artworkId={}, {}", artworkId, e.getMessage());
        }
    }
}
