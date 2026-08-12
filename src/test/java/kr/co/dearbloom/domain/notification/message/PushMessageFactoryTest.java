package kr.co.dearbloom.domain.notification.message;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 알림 문구 검증. 작품명이 포함되되, 개인정보(실명·학교 등)는 들어가지 않아야 한다
 * (잠금화면에 그대로 노출되기 때문 — PushMessageFactory 클래스 주석 참고).
 */
class PushMessageFactoryTest {
    private final PushMessageFactory factory = new PushMessageFactory();
    private final LocalDate shootDate = LocalDate.of(2026, 6, 11);
    private final LocalTime startTime = LocalTime.of(10, 0);

    @Test
    void 문의_생성_알림에_작품명과_촬영일시가_포함된다() {
        PushMessage message = factory.inquiryCreated(1L, "블루밍데이즈 스냅", shootDate, startTime);

        assertThat(message.title()).isEqualTo("새 문의가 도착했어요");
        assertThat(message.body()).isEqualTo("[블루밍데이즈 스냅] 6/11 10:00 촬영 문의예요.");
        assertThat(message.deepLink()).isEqualTo("/app/artist/requests/1");
    }

    @Test
    void 예약_완료_알림에_작품명과_촬영일시가_포함된다() {
        PushMessage message = factory.inquiryReserved(2L, "우정스냅 필름", shootDate, startTime);

        assertThat(message.title()).isEqualTo("예약이 확정됐어요");
        assertThat(message.body()).isEqualTo("[우정스냅 필름] 6/11 10:00 촬영이 예약 완료됐어요.");
        assertThat(message.deepLink()).isEqualTo("/app/my/inquiries/2");
    }
}
