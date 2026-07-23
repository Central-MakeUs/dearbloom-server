package kr.co.dearbloom.domain.inquiry.entity.inquiry;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 문의 라이프사이클 4상태(단일 소스). 결제가 없어 예약 완료가 종착 성공 상태다.
 * 예약 완료 시 슬롯이 잠기고, 예약 취소 시 슬롯이 열린다. 상태 변경 이력은 InquiryHistory 로 남긴다.
 */
@Getter
@AllArgsConstructor
public enum InquiryStatus {
    IN_PROGRESS("문의 진행중"),          // 문의 제출 상태(기본값)
    INQUIRY_CANCELED("문의 취소"),        // 고객/작가가 문의 취소
    RESERVED("예약 완료"),                // 작가가 예약 완료 → 슬롯 잠김
    RESERVATION_CANCELED("예약 취소");    // 작가가 예약 취소 → 슬롯 열림

    private final String label;
}
