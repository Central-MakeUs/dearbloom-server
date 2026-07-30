package kr.co.dearbloom.domain.report.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 신고 대상 종류. 값을 추가할 때는 {@link Report} 에 해당 대상의 nullable FK 필드와
 * 정적 팩토리를 함께 추가한다(대상 컬럼은 타입당 하나만 채워지는 배타적 참조).
 */
@Getter
@AllArgsConstructor
public enum ReportTargetType {
    ARTWORK("작품"),
    CHAT_MESSAGE("채팅 메시지");

    private final String label;
}
