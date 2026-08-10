package kr.co.dearbloom.domain.artwork.dto;

import kr.co.dearbloom.domain.artist.entity.artist.Region;
import kr.co.dearbloom.domain.artwork.dto.type.ArtworkSortOrder;

import java.time.DayOfWeek;
import java.util.Set;

/**
 * 요청 파라미터를 쿼리에 바로 얹을 수 있게 해석해 둔 목록 조회 조건.
 * 날짜 범위 → 요일 집합 변환이나 예약 오픈 창 보정 같은 계산은 여기 담기기 전에 끝난다.
 *
 * @param availableDayOfWeeks 작가가 촬영 가능해야 하는 요일들. <b>null 이면 날짜 필터 없음,
 *                            비어 있으면 조건을 만족하는 날이 아예 없다는 뜻</b>(→ 결과 0건).
 * @param region              촬영 지역. null 이면 지역 필터 없음.
 * @param headCount           촬영 희망 인원. null 이면 인원 필터 없음.
 * @param sort                정렬 기준.
 */
public record ArtworkFilterCondition(
        Set<DayOfWeek> availableDayOfWeeks,
        Region region,
        Integer headCount,
        ArtworkSortOrder sort
) {
}
