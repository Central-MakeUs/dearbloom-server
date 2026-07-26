package kr.co.dearbloom.domain.chat.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 채팅 메시지 종류. TEXT=텍스트, IMAGE=사진(image_url), INQUIRY=문의 카드(문의 생성 시 자동 append, inquiry_id 참조). */
@Getter
@AllArgsConstructor
public enum ChatMessageType {
    TEXT("텍스트"),
    IMAGE("이미지"),
    INQUIRY("문의 카드");

    private final String label;
}
