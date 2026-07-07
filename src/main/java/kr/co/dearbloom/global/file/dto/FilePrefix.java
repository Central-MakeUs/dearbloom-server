package kr.co.dearbloom.global.file.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 업로드 파일 종류(= S3 저장 폴더). 자유 문자열 대신 enum 으로 고정해 폴더 난립을 막는다.
 * folder 값이 실제 S3 key prefix 로 쓰인다.
 */
@Getter
@AllArgsConstructor
public enum FilePrefix {
    REVIEW("review", "리뷰 파일"),
    PORTFOLIO("portfolio", "작품 파일"),
    ARTIST_PROFILE("profile/artist", "작가 프로필 이미지");

    private final String folder;
    private final String label;
}
