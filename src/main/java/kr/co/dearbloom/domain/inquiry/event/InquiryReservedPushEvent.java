package kr.co.dearbloom.domain.inquiry.event;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 문의가 예약 완료(RESERVED)로 전환됐을 때 발행. 수신자는 해당 <b>고객</b>.
 *
 * <p>{@link InquiryCreatedPushEvent} 와 같은 이유로 엔티티가 아닌 값만 담는다.
 */
public record InquiryReservedPushEvent(
        Long inquiryId,
        Long customerMemberId,
        String artworkName,
        LocalDate shootDate,
        LocalTime startTime
) {
}
