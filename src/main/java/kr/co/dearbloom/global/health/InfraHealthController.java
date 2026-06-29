package kr.co.dearbloom.global.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static kr.co.dearbloom.global.health.HealthStatus.CONNECTED;
import static kr.co.dearbloom.global.health.HealthStatus.DISCONNECTED;

@RestController
@RequestMapping("/health/infra")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "HealthCheck Infra", description = "인프라(DB, Redis, Kafka 등) 연결 확인")
public class InfraHealthController {
    private final DataSource dataSource;
    private final RedisConnectionFactory redisConnectionFactory;
    private final StringRedisTemplate stringRedisTemplate;

    // ─────────────────── MySQL ───────────────────

    @Operation(summary = "MySQL 연결 헬스체크")
    @GetMapping("/db")
    public ResponseEntity<Map<String, Object>> dbHealthCheck() {
        Map<String, Object> body = new LinkedHashMap<>();
        try (Connection conn = dataSource.getConnection()) {
            boolean valid = conn.isValid(2); // 2초 timeout
            body.put("status", valid ? CONNECTED : DISCONNECTED);
            body.put("database", conn.getMetaData().getDatabaseProductName());
            body.put("url", conn.getMetaData().getURL());
            body.put("checkedAt", ZonedDateTime.now());
            return valid
                    ? ResponseEntity.ok(body)
                    : ResponseEntity.status(503).body(body);
        } catch (Exception e) {
            log.warn("DB health check failed", e);
            body.put("status", DISCONNECTED);
            body.put("error", e.getMessage());
            body.put("checkedAt", ZonedDateTime.now());
            return ResponseEntity.status(503).body(body);
        }
    }

    // ─────────────────── Redis ───────────────────

    @Operation(summary = "Redis 연결 헬스체크")
    @GetMapping("/redis")
    public ResponseEntity<Map<String, Object>> redisHealthCheck() {
        Map<String, Object> body = new LinkedHashMap<>();
        try (RedisConnection conn = redisConnectionFactory.getConnection()) {
            String pong = conn.ping();
            boolean up = "PONG".equalsIgnoreCase(pong);
            body.put("status", up ? CONNECTED : DISCONNECTED);
            body.put("ping", pong);
            body.put("checkedAt", ZonedDateTime.now());
            return up
                    ? ResponseEntity.ok(body)
                    : ResponseEntity.status(503).body(body);
        } catch (Exception e) {
            log.warn("Redis health check failed", e);
            body.put("status", DISCONNECTED);
            body.put("error", e.getMessage());
            body.put("checkedAt", ZonedDateTime.now());
            return ResponseEntity.status(503).body(body);
        }
    }

    @Operation(summary = "Redis 읽기/쓰기 동작 헬스체크",
            description = "임시 키에 값 SET → GET 으로 일치 확인 → DEL 로 정리. PING 보다 강한 검증.")
    @GetMapping("/redis/rw")
    public ResponseEntity<Map<String, Object>> redisReadWriteCheck() {
        Map<String, Object> body = new LinkedHashMap<>();
        String key = "healthcheck:rw:" + UUID.randomUUID();
        String expected = "ok-" + System.currentTimeMillis();

        try {
            // SET (10초 TTL - 만료로 자동 정리되더라도 안전)
            stringRedisTemplate.opsForValue().set(key, expected, Duration.ofSeconds(10));

            // GET
            String actual = stringRedisTemplate.opsForValue().get(key);

            // DEL (즉시 정리)
            Boolean deleted = stringRedisTemplate.delete(key);

            boolean ok = expected.equals(actual);
            body.put("status", ok ? CONNECTED : DISCONNECTED);
            body.put("key", key);
            body.put("expected", expected);
            body.put("actual", actual);
            body.put("deleted", deleted);
            body.put("checkedAt", ZonedDateTime.now());
            return ok
                    ? ResponseEntity.ok(body)
                    : ResponseEntity.status(503).body(body);
        } catch (Exception e) {
            log.warn("Redis R/W health check failed", e);
            body.put("status", DISCONNECTED);
            body.put("key", key);
            body.put("error", e.getMessage());
            body.put("checkedAt", ZonedDateTime.now());
            return ResponseEntity.status(503).body(body);
        }
    }
}
