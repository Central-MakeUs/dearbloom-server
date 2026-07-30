package kr.co.dearbloom.domain.report.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 신고 처리 상태. 접수(기본) → 어드민이 반려 또는 처리완료로 전이시킨다. */
@Getter
@AllArgsConstructor
public enum ReportStatus {
    RECEIVED("접수"),
    REJECTED("반려"),
    RESOLVED("처리완료");

    private final String label;
}
