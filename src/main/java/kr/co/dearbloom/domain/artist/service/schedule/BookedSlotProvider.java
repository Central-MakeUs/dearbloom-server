package kr.co.dearbloom.domain.artist.service.schedule;

import java.time.LocalDate;
import java.util.Map;

/**
 * 예약 확정(BOOKED) 슬롯을 가용성 계산에서 빼기 위한 포트(port).
 * 실제 구현은 예약(문의) 도메인이 어댑터로 제공한다 — 여기(artist 도메인)는 인터페이스만 소유.
 */
public interface BookedSlotProvider {
    /** 해당 작가/날짜에 예약 확정된 셀들의 24비트 마스크. 없으면 0. */
    int bookedMask(Long artistId, LocalDate date);

    /** 기간 [from, to] 의 날짜별 예약 확정 마스크(캘린더 배치 조회용, N+1 회피). 없는 날짜는 키 없음. */
    Map<LocalDate, Integer> bookedMasks(Long artistId, LocalDate from, LocalDate to);
}
