package kr.co.dearbloom.domain.university.repository;

import kr.co.dearbloom.domain.university.entity.University;
import kr.co.dearbloom.global.util.HangulUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class UniversityAutocompleteRepository {
    private static final String KEY = "university:autocomplete";
    private static final String DELIMITER = "|";

    private final StringRedisTemplate redisTemplate;

    public void buildIndex(List<University> universities) {
        redisTemplate.delete(KEY);
        Set<ZSetOperations.TypedTuple<String>> tuples = universities.stream()
                .map(u -> ZSetOperations.TypedTuple.of(
                        u.getName() + DELIMITER
                                + u.getCampusType() + DELIMITER
                                + u.getRegion() + DELIMITER
                                + u.getUniversityId(),
                        0.0))
                .collect(Collectors.toSet());
        if (!tuples.isEmpty()) {
            redisTemplate.opsForZSet().add(KEY, tuples);
        }
    }

    public List<String> search(String query, int limit) {
        // 자음(초성)이 나오기 전까지의 완성형 prefix 로 Redis 후보를 1차 축소.
        String prefix = HangulUtils.leadingCompletePrefix(query);
        Set<String> candidates = (prefix.isEmpty())
                // 첫 글자부터 자음 → lex 범위로 못 좁히므로 전체 조회 (데이터 소량)
                ? redisTemplate.opsForZSet().range(KEY, 0, -1)
                : redisTemplate.opsForZSet().rangeByLex(KEY, Range.closed(prefix, prefix + "￿"));

        if (candidates == null) {
            return List.of();
        }
        return candidates.stream()
                .filter(member -> HangulUtils.matchesPrefix(nameOf(member), query))
                .limit(Math.max(0, limit)) // 음수 limit → Stream.limit IllegalArgumentException(500) 방지
                .toList();
    }

    /** 멤버("이름|캠퍼스|지역|ID")에서 대학명만 추출. */
    private String nameOf(String member) {
        int idx = member.indexOf(DELIMITER);
        return idx >= 0 ? member.substring(0, idx) : member;
    }
}
