package kr.co.dearbloom.domain.notification.message;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 알림 종류별 문구와 딥링크를 한 곳에 모은다.
 *
 * <p>본문에 실명·학교명 같은 개인정보를 넣지 않는다 — 잠금화면에 그대로 뜨기 때문이다.
 * 누가 보냈는지가 아니라 <b>언제 촬영인지</b>만 담는다.
 */
@Component
public class PushMessageFactory {
    private static final DateTimeFormatter SCHEDULE = DateTimeFormatter.ofPattern("M/d HH:mm");

    /** 문의가 새로 생성됨 → 해당 작가에게. */
    public PushMessage inquiryCreated(
            Long inquiryId, String artworkName, LocalDate shootDate, LocalTime startTime) {
        return PushMessage.of(
                "새 문의가 도착했어요",
                "[" + artworkName + "] " + schedule(shootDate, startTime) + " 촬영 문의예요.",
                "/app/artist/requests/" + inquiryId);
    }

    /** 문의가 예약 완료로 전환됨 → 해당 고객에게. */
    public PushMessage inquiryReserved(
            Long inquiryId, String artworkName, LocalDate shootDate, LocalTime startTime) {
        return PushMessage.of(
                "예약이 확정됐어요",
                "[" + artworkName + "] " + schedule(shootDate, startTime) + " 촬영이 예약 완료됐어요.",
                "/app/my/inquiries/" + inquiryId);
    }

    private String schedule(LocalDate shootDate, LocalTime startTime) {
        return shootDate.atTime(startTime).format(SCHEDULE);
    }
}
