package kr.co.dearbloom.global.file.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FilePrefix {
    REVIEW("review", "리뷰 파일"),
    PORTFOLIO("portfolio", "작품 파일"),
    ARTIST_PROFILE("profile/artist", "작가 프로필 이미지");

    private final String folder;
    private final String label;
}
