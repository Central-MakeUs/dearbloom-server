package kr.co.dearbloom.domain.artist.service.schedule;

import java.time.LocalDate;

/**
 * 예약 확정(BOOKED) 슬롯을 가용성 계산에서 빼기 위한 주입 지점.
 * 예약 도메인은 아직 기획단계라 지금은 {@link NoOpBookedSlotProvider}(전부 0) 가 바인딩된다.
 * 예약 기능이 붙으면 CONFIRMED 예약을 조회해 마스크로 돌려주는 구현으로 교체한다.
 */
public interface BookedSlotProvider {
    /** 해당 작가/날짜에 이미 예약 확정된 셀들의 24비트 마스크. 없으면 0. */
    int bookedMask(Long artistId, LocalDate date);
}
