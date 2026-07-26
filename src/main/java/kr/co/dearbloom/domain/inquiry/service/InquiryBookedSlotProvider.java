package kr.co.dearbloom.domain.inquiry.service;

import kr.co.dearbloom.domain.artist.service.schedule.BookedSlotProvider;
import kr.co.dearbloom.domain.artist.util.SlotGrid;
import kr.co.dearbloom.domain.inquiry.entity.Inquiry;
import kr.co.dearbloom.domain.inquiry.entity.InquiryStatus;
import kr.co.dearbloom.domain.inquiry.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 예약 확정(RESERVED) 상태 문의를 읽어 예약된 셀 마스크를 계산하는 {@link BookedSlotProvider} 어댑터.
 * 예약 완료 시 status=RESERVED 가 되어 booked 로 잡히고, 예약 취소 시 status 가 바뀌어 자동으로 빠진다(슬롯 열림).
 */
@Component
@RequiredArgsConstructor
public class InquiryBookedSlotProvider implements BookedSlotProvider {
    private final InquiryRepository inquiryRepository;

    @Override
    public int bookedMask(Long artistId, LocalDate date) {
        return maskOf(inquiryRepository.findByArtistAndShootDateAndStatus(
                artistId, date, InquiryStatus.RESERVED));
    }

    @Override
    public Map<LocalDate, Integer> bookedMasks(Long artistId, LocalDate from, LocalDate to) {
        Map<LocalDate, Integer> byDate = new HashMap<>();
        for (Inquiry inquiry : inquiryRepository.findByArtistAndShootDateBetweenAndStatus(
                artistId, from, to, InquiryStatus.RESERVED)) {
            byDate.merge(inquiry.getShootDate(), rangeMaskOf(inquiry), (a, b) -> a | b);
        }
        return byDate;
    }

    private int maskOf(List<Inquiry> inquiries) {
        int mask = 0;
        for (Inquiry inquiry : inquiries) {
            mask |= rangeMaskOf(inquiry);
        }
        return mask;
    }

    // 문의의 [시작시각, 시작시각+소요시간) 을 셀 마스크로.
    private int rangeMaskOf(Inquiry inquiry) {
        return SlotGrid.rangeMask(
                inquiry.getStartTime(),
                inquiry.getStartTime().plusMinutes(inquiry.getDurationMinutesSnapshot()));
    }
}
