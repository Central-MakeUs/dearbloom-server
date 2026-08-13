package kr.co.dearbloom.domain.artwork.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.dearbloom.domain.artist.entity.artist.QArtist;
import kr.co.dearbloom.domain.artist.entity.artist.Region;
import kr.co.dearbloom.domain.artist.entity.schedule.QArtistScheduleRule;
import kr.co.dearbloom.domain.artist.entity.schedule.ScheduleRuleType;
import kr.co.dearbloom.domain.artwork.dto.ArtworkCursor;
import kr.co.dearbloom.domain.artwork.dto.ArtworkFilterCondition;
import kr.co.dearbloom.domain.artwork.dto.type.ArtworkSortOrder;
import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.domain.artwork.entity.QArtwork;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Set;

/** 작품 탐색 목록(필터·정렬·커서 페이지네이션) 전용 조회. */
@RequiredArgsConstructor
@Repository
public class ArtworkQueryRepository {
    private final JPAQueryFactory jpaQueryFactory;

    /**
     * 필터·정렬을 적용한 작품 페이지.
     * hasNext 판단용으로 size + 1 개를 가져오고, 자르는 건 호출부 책임이다.
     * 작가는 fetch join 으로 함께 가져오고 작가 활동지역은 Artist.regions 의 @BatchSize 로 묶인다.
     */
    public List<Artwork> findArtworkPage(ArtworkFilterCondition condition, ArtworkCursor cursor, int size) {
        QArtwork artwork = QArtwork.artwork;

        BooleanBuilder where = filterConditions(condition)
                .and(cursorCondition(condition.sort(), cursor)); // null 이면 무시됨

        return jpaQueryFactory
                .selectFrom(artwork)
                .join(artwork.artist, QArtist.artist).fetchJoin()
                .where(where)
                .orderBy(orderSpecifiers(condition.sort()))
                .limit(size + 1L) // hasNext 판단용 +1
                .fetch();
    }

    /** 같은 필터를 적용한 전체 개수. 정렬·커서는 개수에 영향이 없으므로 뺀다. */
    public long countArtworks(ArtworkFilterCondition condition) {
        QArtwork artwork = QArtwork.artwork;

        Long count = jpaQueryFactory
                .select(artwork.count())
                .from(artwork)
                .where(filterConditions(condition))
                .fetchOne();

        return count != null ? count : 0L;
    }

    // 페이지 조회와 개수 조회가 같은 필터를 보도록 한 곳에서 만든다(둘이 어긋나면 총 개수가 거짓말을 한다).
    private BooleanBuilder filterConditions(ArtworkFilterCondition condition) {
        return new BooleanBuilder()
                .and(availableDayCondition(condition.availableDayOfWeeks()))
                .and(regionCondition(condition.region()))
                .and(headCountCondition(condition.headCount()));
    }

    /**
     * 날짜 필터 — 작가가 이 요일들 중 하루라도 촬영 가능한가.
     * 요청이 "연속 N일"이라 날짜 범위를 요일 집합으로 접을 수 있고(7일 이상이면 7요일 전부),
     * 덕분에 30일치 날짜를 IN 절에 늘어놓지 않고 최대 7개짜리 EXISTS 하나로 끝난다.
     *
     * <p>요일 규칙(WEEKLY_AVAILABLE)만 본다 — DATE_BLOCK/예약 확정으로 특정 날짜가 통째로 막힌 경우는
     * 걸러내지 않는다. 구간 뺄셈이라 SQL 로 표현하기 어렵고, 기간 필터에선 오차가 사실상 없다.
     * 하루만 고른 경우엔 "그날 통으로 비워둔 작가"가 노출될 수 있다.
     */
    private BooleanExpression availableDayCondition(Set<DayOfWeek> dayOfWeeks) {
        if (dayOfWeeks == null) {
            return null; // 날짜 필터 없음
        }
        if (dayOfWeeks.isEmpty()) {
            return Expressions.asBoolean(false).isTrue(); // 가능한 날이 아예 없음 → 0건
        }
        QArtistScheduleRule rule = QArtistScheduleRule.artistScheduleRule;
        return JPAExpressions.selectOne()
                .from(rule)
                .where(rule.artist.artistId.eq(QArtwork.artwork.artist.artistId),
                        rule.ruleType.eq(ScheduleRuleType.WEEKLY_AVAILABLE),
                        rule.dayOfWeek.in(dayOfWeeks))
                .exists();
    }

    /**
     * 지역 필터 — 작가 활동지역에 이 지역이 들어있는가.
     * regions 는 @ElementCollection 이라 바깥에서 조인하면 행이 복제돼 distinct 가 필요해지고,
     * 그러면 ORDER BY + LIMIT 의 조기 종료가 깨진다. 그래서 EXISTS 서브쿼리 안에서만 조인한다.
     * (다중 선택으로 바뀌면 eq → in 만 바꾸면 된다.)
     */
    private BooleanExpression regionCondition(Region region) {
        if (region == null) {
            return null;
        }
        QArtist regionArtist = new QArtist("regionArtist");
        EnumPath<Region> artistRegion = Expressions.enumPath(Region.class, "artistRegion");
        return JPAExpressions.selectOne()
                .from(regionArtist)
                .join(regionArtist.regions, artistRegion)
                .where(regionArtist.artistId.eq(QArtwork.artwork.artist.artistId),
                        artistRegion.eq(region))
                .exists();
    }

    /**
     * 인원 필터 — 희망 인원이 작품의 촬영 가능 인원 범위에 들어가는가.
     * maxHeadCount 가 null 이면 "N인 이상"(상한 없음)이라 위쪽은 항상 통과한다.
     * headCount = 6("6인 이상")도 min ≤ 6 이 늘 참이라 같은 식으로 처리된다.
     */
    private BooleanExpression headCountCondition(Integer headCount) {
        if (headCount == null) {
            return null;
        }
        QArtwork artwork = QArtwork.artwork;
        return artwork.minHeadCount.loe(headCount)
                .and(artwork.maxHeadCount.isNull()
                        .or(artwork.maxHeadCount.goe(headCount)));
    }

    /**
     * 커서 where 조건. 정렬 튜플 비교를 SQL 로 풀어서 쓴다.
     * 커서가 null 이면(첫 페이지) 조건 없음.
     */
    private BooleanExpression cursorCondition(ArtworkSortOrder sort, ArtworkCursor cursor) {
        if (cursor == null) {
            return null;
        }
        QArtwork artwork = QArtwork.artwork;

        return switch (sort) {
            // (createdAt, artworkId) < (cursor.createdAt, cursor.artworkId)
            case LATEST -> artwork.createdAt.lt(cursor.createdAt())
                    .or(artwork.createdAt.eq(cursor.createdAt())
                            .and(artwork.artworkId.lt(cursor.artworkId())));

            // 가격 ASC, ID DESC (혼합 방향) — 가격이 같으면 ID 가 더 작은 쪽이 뒤에 온다.
            case PRICE_LOW -> artwork.lowestPrice.gt(cursor.lowestPrice())
                    .or(artwork.lowestPrice.eq(cursor.lowestPrice())
                            .and(artwork.artworkId.lt(cursor.artworkId())));

            // 가격 DESC, ID DESC
            case PRICE_HIGH -> artwork.lowestPrice.lt(cursor.lowestPrice())
                    .or(artwork.lowestPrice.eq(cursor.lowestPrice())
                            .and(artwork.artworkId.lt(cursor.artworkId())));
        };
    }

    // 정렬 키가 같은 행의 순서가 흔들리면 커서 페이지네이션이 행을 빠뜨리거나 중복시키므로 artworkId 를 항상 마지막에 둔다.
    private OrderSpecifier<?>[] orderSpecifiers(ArtworkSortOrder sort) {
        QArtwork artwork = QArtwork.artwork;

        return switch (sort) {
            case LATEST -> new OrderSpecifier<?>[]{
                    artwork.createdAt.desc(),
                    artwork.artworkId.desc()
            };
            case PRICE_LOW -> new OrderSpecifier<?>[]{
                    artwork.lowestPrice.asc(),
                    artwork.artworkId.desc()
            };
            case PRICE_HIGH -> new OrderSpecifier<?>[]{
                    artwork.lowestPrice.desc(),
                    artwork.artworkId.desc()
            };
        };
    }
}
