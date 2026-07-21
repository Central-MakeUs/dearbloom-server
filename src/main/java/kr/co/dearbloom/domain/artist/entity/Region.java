package kr.co.dearbloom.domain.artist.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

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
}
