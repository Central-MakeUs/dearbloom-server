package kr.co.dearbloom.domain.artist.entity.artist;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collection;
import java.util.List;

@Getter
@AllArgsConstructor
public enum Region {
    SEOUL("서울"),
    GYEONGGI_NORTH("경기 북부"),
    GYEONGGI_SOUTH("경기 남부"),
    INCHEON("인천"),
    BUSAN("부산"),
    DAEGU("대구"),
    GWANGJU("광주"),
    DAEJEON_SEJONG("대전/세종"),
    ULSAN("울산"),
    GANGWON("강원"),
    CHUNGBUK("충북"),
    CHUNGNAM("충남"),
    JEONBUK("전북"),
    JEONNAM("전남"),
    GYEONGBUK("경북"),
    GYEONGNAM("경남"),
    JEJU("제주");

    private final String label;

    /**
     * 응답용 지역 코드 목록. 위 선언 순서(수도권 → 광역시 → 도)대로 정렬한다.
     * Artist.regions 가 HashSet 이라 순회 순서가 정해져 있지 않으므로, 응답은 항상 이 메서드를 통해 만든다.
     */
    public static List<String> toSortedNames(Collection<Region> regions) {
        return regions.stream().sorted().map(Enum::name).toList();
    }
}
