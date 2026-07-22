package kr.co.dearbloom.domain.artist.service.schedule;

import org.springframework.stereotype.Component;

import java.time.LocalDate;

/** 예약 도메인 연동 전 기본 구현. 예약 확정 슬롯이 없다고 본다(마스크 0). */
@Component
public class NoOpBookedSlotProvider implements BookedSlotProvider {
    @Override
    public int bookedMask(Long artistId, LocalDate date) {
        return 0;
    }
}
