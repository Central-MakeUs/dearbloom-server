package kr.co.dearbloom.domain.artwork.service;

import kr.co.dearbloom.domain.artist.entity.artist.Region;
import kr.co.dearbloom.domain.artwork.dto.ArtworkFilterCondition;
import kr.co.dearbloom.domain.artwork.dto.request.ArtworkQueryRequest;
import kr.co.dearbloom.domain.artwork.dto.type.ArtworkSortOrder;
import kr.co.dearbloom.global.dto.response.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 날짜 범위 → 요일 집합 접기 검증.
 * 이 변환이 날짜 필터의 전부라, 30일치 날짜가 쿼리로 새어 나가지 않는지도 여기서 확인한다.
 * 조건 해석은 저장소를 전혀 건드리지 않으므로 의존성은 null 로 둔다.
 */
class ArtworkQueryServiceConditionTest {
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final ArtworkQueryService artworkQueryService =
            new ArtworkQueryService(null, null, null, null);

    private ArtworkFilterCondition resolve(LocalDate startDate, LocalDate endDate) {
        ArtworkQueryRequest request = new ArtworkQueryRequest();
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        return artworkQueryService.resolveCondition(request);
    }

    private LocalDate today() {
        return LocalDate.now(KST);
    }

    @Test
    @DisplayName("날짜를 안 보내면 날짜 필터가 없다(null)")
    void 날짜_없으면_필터_없음() {
        assertThat(resolve(null, null).availableDayOfWeeks()).isNull();
    }

    @Test
    @DisplayName("하루만 고르면 그 하루의 요일만 남는다")
    void 하루는_요일_하나() {
        LocalDate day = today().plusDays(3);

        assertThat(resolve(day, day).availableDayOfWeeks())
                .containsExactly(day.getDayOfWeek());
    }

    @Test
    @DisplayName("오늘부터 7일이면 7요일이 전부 들어간다 — 이래서 날짜 개수가 쿼리 비용에 영향을 주지 않는다")
    void 일주일이면_모든_요일() {
        assertThat(resolve(today(), today().plusDays(7)).availableDayOfWeeks())
                .containsExactlyInAnyOrder(DayOfWeek.values());
    }

    @Test
    @DisplayName("30일을 골라도 요일 7개로 접힌다(날짜 30개가 쿼리로 나가지 않는다)")
    void 삼십일도_요일_일곱개() {
        assertThat(resolve(today(), today().plusDays(30)).availableDayOfWeeks())
                .hasSize(7);
    }

    @Test
    @DisplayName("시작일이 과거면 예약 오픈 시작일(오늘)로 당겨서 계산한다")
    void 과거_시작일은_오늘로_보정() {
        // [어제, 오늘] → 오늘 하루로 좁혀지므로 어제의 요일은 빠진다.
        assertThat(resolve(today().minusDays(1), today()).availableDayOfWeeks())
                .containsExactly(today().getDayOfWeek());
    }

    @Test
    @DisplayName("예약 오픈 창(오늘~3개월) 을 완전히 벗어난 기간은 매칭되는 날이 없다(빈 집합)")
    void 오픈창_밖은_빈_집합() {
        LocalDate farPast = today().minusDays(20);

        assertThat(resolve(farPast, farPast.plusDays(5)).availableDayOfWeeks()).isEmpty();
    }

    @Test
    @DisplayName("종료일이 시작일보다 빠르면 400")
    void 뒤집힌_기간은_예외() {
        assertThatThrownBy(() -> resolve(today().plusDays(5), today().plusDays(1)))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("한쪽 날짜만 보내면 400")
    void 한쪽만_보내면_예외() {
        assertThatThrownBy(() -> resolve(today(), null)).isInstanceOf(CustomException.class);
        assertThatThrownBy(() -> resolve(null, today())).isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("30일을 넘는 기간은 400")
    void 최대_기간_초과는_예외() {
        assertThatThrownBy(() -> resolve(today(), today().plusDays(31)))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("지역·인원·정렬은 받은 값이 그대로 조건에 실린다")
    void 나머지_조건은_그대로_전달() {
        ArtworkQueryRequest request = new ArtworkQueryRequest();
        request.setRegion(Region.SEOUL);
        request.setHeadCount(2);
        request.setSort(ArtworkSortOrder.PRICE_LOW);

        ArtworkFilterCondition condition = artworkQueryService.resolveCondition(request);

        assertThat(condition.region()).isEqualTo(Region.SEOUL);
        assertThat(condition.headCount()).isEqualTo(2);
        assertThat(condition.sort()).isEqualTo(ArtworkSortOrder.PRICE_LOW);
    }

    @Test
    @DisplayName("sort 가 빈 값으로 오면 기본값(LATEST)이 유지된다")
    void 빈_값이_와도_기본값_유지() {
        ArtworkQueryRequest request = new ArtworkQueryRequest();
        request.setSort(null);

        assertThat(request.getSort()).isEqualTo(ArtworkSortOrder.LATEST);
    }
}
