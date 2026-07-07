package kr.co.dearbloom.global.dev.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/dev/infra")
@Tag(name = "Dev - Infra", description = "[백엔드 전용] 외부 인프라 테스트 API")
public class DevInfraController {
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 사용자가 지정한 key/value/TTL 로 Redis 에 값을 저장하고 즉시 GET 으로 확인.
     * 헬스체크와 달리 <b>삭제하지 않음</b> — RedisInsight 등으로 직접 확인하거나 TTL 만료까지 유지.
     *
     * @param ttlSeconds 만료 시간(초). 0 이하면 만료 없이(persist) 저장.
     */
    @Operation(summary = "Redis 임의 키 저장 (삭제 안 함)",
            description = "입력한 key/value 를 지정 TTL 로 SET 후 GET 으로 확인. 삭제하지 않으므로 TTL 만료 전까지 Redis 에 남음. " +
                    "ttlSeconds 0 이하면 만료 없이 저장.")
    @PostMapping("/redis/set")
    public ResponseEntity<Map<String, Object>> redisSet(
            @RequestParam String key,
            @RequestParam String value,
            @RequestParam(defaultValue = "0") long ttlSeconds
    ) {
        Map<String, Object> body = new LinkedHashMap<>();
        try {
            if (ttlSeconds > 0) {
                stringRedisTemplate.opsForValue().set(key, value, Duration.ofSeconds(ttlSeconds));
            } else {
                stringRedisTemplate.opsForValue().set(key, value); // 만료 없음
            }

            String actual = stringRedisTemplate.opsForValue().get(key);
            Long ttl = stringRedisTemplate.getExpire(key); // 남은 TTL(초). -1 만료없음, -2 없음

            boolean ok = value.equals(actual);
            body.put("ok", ok);
            body.put("key", key);
            body.put("value", value);
            body.put("actual", actual);
            body.put("ttlSeconds", ttl);
            body.put("savedAt", ZonedDateTime.now());
            return ok
                    ? ResponseEntity.ok(body)
                    : ResponseEntity.status(503).body(body);
        } catch (Exception e) {
            log.warn("Redis dev set failed. key={}", key, e);
            body.put("ok", false);
            body.put("key", key);
            body.put("error", e.getMessage());
            body.put("savedAt", ZonedDateTime.now());
            return ResponseEntity.status(503).body(body);
        }
    }
}
