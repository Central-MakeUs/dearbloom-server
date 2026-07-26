package kr.co.dearbloom.domain.inquiry.service;

import kr.co.dearbloom.domain.artist.repository.ArtistRepository;
import kr.co.dearbloom.domain.customer.repository.CustomerRepository;
import kr.co.dearbloom.domain.inquiry.entity.Inquiry;
import kr.co.dearbloom.domain.inquiry.entity.InquiryStatus;
import kr.co.dearbloom.domain.inquiry.repository.InquiryRepository;
import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.domain.member.entity.MemberRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 회원 탈퇴·역할 해지 시 나가는 당사자에게 걸린 진행 중 문의를 자동 정리한다.
 * 진행중(IN_PROGRESS) → 문의 취소, 예약 완료(RESERVED) → 예약 취소. 예약 취소로 슬롯은 자동 해제된다.
 * 각 전이는 나가는 당사자의 role 과 시스템 사유를 InquiryHistory 에 남긴다(상대방이 취소 이유를 audit 으로 확인).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class InquiryWithdrawalService {
    private static final List<InquiryStatus> ACTIVE = List.of(InquiryStatus.IN_PROGRESS, InquiryStatus.RESERVED);
    private static final String REASON_WITHDRAWAL = "회원 탈퇴로 자동 취소";
    private static final String REASON_CUSTOMER_REVOKE = "고객 역할 해지로 자동 취소";
    private static final String REASON_ARTIST_REVOKE = "작가 역할 해지로 자동 취소";

    private final InquiryRepository inquiryRepository;
    private final InquiryHistoryCommandService historyCommandService;
    private final CustomerRepository customerRepository;
    private final ArtistRepository artistRepository;

    /** 회원 탈퇴: 이 회원이 고객·작가 어느 쪽으로든 걸린 진행중/예약완료 문의를 모두 자동 취소. */
    public void cancelAllForWithdrawal(Member member) {
        customerRepository.findByMember(member).ifPresent(c ->
                cancelCustomerSide(c.getCustomerId(), REASON_WITHDRAWAL));
        artistRepository.findByMember(member).ifPresent(a ->
                cancelArtistSide(a.getArtistId(), REASON_WITHDRAWAL));
    }

    /** 고객 역할 해지: 고객으로 걸린 진행중/예약완료 문의만 자동 취소. */
    public void cancelForCustomerRevoke(Member member) {
        customerRepository.findByMember(member).ifPresent(c ->
                cancelCustomerSide(c.getCustomerId(), REASON_CUSTOMER_REVOKE));
    }

    /** 작가 역할 해지: 작가로 걸린 진행중/예약완료 문의만 자동 취소. */
    public void cancelForArtistRevoke(Member member) {
        artistRepository.findByMember(member).ifPresent(a ->
                cancelArtistSide(a.getArtistId(), REASON_ARTIST_REVOKE));
    }

    private void cancelCustomerSide(Long customerId, String reason) {
        inquiryRepository.findByCustomerIdAndStatusIn(customerId, ACTIVE)
                .forEach(inquiry -> cancelOne(inquiry, MemberRole.CUSTOMER, reason));
    }

    private void cancelArtistSide(Long artistId, String reason) {
        inquiryRepository.findByArtistIdAndStatusIn(artistId, ACTIVE)
                .forEach(inquiry -> cancelOne(inquiry, MemberRole.ARTIST, reason));
    }

    private void cancelOne(Inquiry inquiry, MemberRole actor, String reason) {
        InquiryStatus from = inquiry.getStatus();
        switch (from) {
            case IN_PROGRESS -> inquiry.cancelAsInquiry();
            case RESERVED -> inquiry.cancelReservation();
            default -> { return; } // 이미 종료 상태면 방어적으로 스킵
        }
        historyCommandService.record(inquiry, from, actor, reason);
    }
}
