package kr.co.dearbloom.global.file.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FilePrefix {
    REVIEW("review", "리뷰 파일"),
    PORTFOLIO("portfolio", "작품 파일"),
    ARTIST_IMAGE("artist/image", "작가 이미지"),
    CHAT_IMAGE("chat/image", "채팅 이미지");

    private final String folder;
    private final String label;
}
