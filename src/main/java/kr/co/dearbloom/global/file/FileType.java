package kr.co.dearbloom.global.file;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FileType {
    IMAGE("이미지"),
    VIDEO("동영상"),
    DOCUMENT("문서");

    private final String label;
}
