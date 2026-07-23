package kr.co.dearbloom.domain.inquiry.facade;

import kr.co.dearbloom.domain.artist.entity.artist.Artist;
import kr.co.dearbloom.domain.artist.service.schedule.BookedSlotProvider;
import kr.co.dearbloom.domain.artist.util.SlotGrid;
import kr.co.dearbloom.domain.artwork.service.ArtworkQueryService;
import kr.co.dearbloom.domain.inquiry.dto.response.InquiryHistoryResponse;
import kr.co.dearbloom.domain.inquiry.dto.response.InquiryStatusResponse;
import kr.co.dearbloom.domain.inquiry.dto.response.artist.ArtistInquiryDetailResponse;
import kr.co.dearbloom.domain.inquiry.dto.response.artist.ArtistInquiryListItemResponse;
import kr.co.dearbloom.domain.inquiry.entity.inquiry.Inquiry;
import kr.co.dearbloom.domain.inquiry.entity.inquiry.InquiryStatus;
import kr.co.dearbloom.domain.inquiry.service.InquiryHistoryCommandService;
import kr.co.dearbloom.domain.inquiry.service.InquiryQueryService;
import kr.co.dearbloom.domain.member.entity.MemberRole;
import kr.co.dearbloom.global.dto.response.exception.CustomException;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 작가 관점 문의 유스케이스 (리스트/상세 + 상태 전이: 문의취소/예약완료/예약취소). 본인 작품의 문의만 다룬다. */
@Component
@RequiredArgsConstructor
public class ArtistInquiryFacade {
    private final InquiryQueryService inquiryQueryService;
    private final InquiryHistoryCommandService inquiryHistoryCommandService;
    private final ArtworkQueryService artworkQueryService;
    private final BookedSlotProvider bookedSlotProvider;

    /** 작가 작품에 들어온 문의 리스트(촬영일 오름차순). 리스트엔 이미지 없음. */
    @Transactional(readOnly = true)
    public List<ArtistInquiryListItemResponse> getInquiries(Artist artist) {
        return inquiryQueryService.getByArtist(artist.getArtistId()).stream()
                .map(ArtistInquiryListItemResponse::of)
                .toList();
    }

    /** 작가 문의 상세. 본인 작품의 문의만. */
    @Transactional(readOnly = true)
    public ArtistInquiryDetailResponse getInquiryDetail(Artist artist, Long inquiryId) {
        Inquiry inquiry = inquiryQueryService.getById(inquiryId);
        verifyArtistOwns(inquiry, artist);
        String imageUrl = artworkQueryService.getRepresentativeImageUrl(inquiry.getArtworkPackage().getArtwork());
        return ArtistInquiryDetailResponse.of(inquiry, imageUrl);
    }

    /** 문의 상태 변경 이력(타임라인, 오래된 순). 본인 작품의 문의만. */
    @Transactional(readOnly = true)
    public List<InquiryHistoryResponse> getInquiryHistory(Artist artist, Long inquiryId) {
        Inquiry inquiry = inquiryQueryService.getById(inquiryId);
        verifyArtistOwns(inquiry, artist);
        return inquiryQueryService.getHistory(inquiryId).stream()
                .map(InquiryHistoryResponse::of)
                .toList();
    }

    /** 문의 취소(작가). 본인 작품의 문의 + 진행중일 때만. */
    @Transactional
    public InquiryStatusResponse cancelInquiry(Artist artist, Long inquiryId) {
        Inquiry inquiry = inquiryQueryService.getById(inquiryId);
        verifyArtistOwns(inquiry, artist);
        InquiryStatus from = inquiry.getStatus();
        inquiry.cancelAsInquiry();
        inquiryHistoryCommandService.record(inquiry, from, MemberRole.ARTIST);
        return InquiryStatusResponse.of(inquiry);
    }

    /** 예약 완료(작가). 진행중 → 예약완료. 다른 예약과 겹치지 않는지 재검증 후 상태 전환(슬롯 잠금 = RESERVED). */
    @Transactional
    public InquiryStatusResponse completeReservation(Artist artist, Long inquiryId) {
        Inquiry inquiry = inquiryQueryService.getById(inquiryId);
        Artist owner = verifyArtistOwns(inquiry, artist);
        validateNotBooked(owner, inquiry);
        InquiryStatus from = inquiry.getStatus();
        inquiry.reserve();
        inquiryHistoryCommandService.record(inquiry, from, MemberRole.ARTIST);
        return InquiryStatusResponse.of(inquiry);
    }

    /** 예약 취소(작가). 예약완료 → 예약취소. status 변경만으로 슬롯이 열린다. */
    @Transactional
    public InquiryStatusResponse cancelReservation(Artist artist, Long inquiryId) {
        Inquiry inquiry = inquiryQueryService.getById(inquiryId);
        verifyArtistOwns(inquiry, artist);
        InquiryStatus from = inquiry.getStatus();
        inquiry.cancelReservation();
        inquiryHistoryCommandService.record(inquiry, from, MemberRole.ARTIST);
        return InquiryStatusResponse.of(inquiry);
    }

    // 문의가 이 작가(작품)의 것인지 검증하고, tx-managed 작가 엔티티를 반환.
    private Artist verifyArtistOwns(Inquiry inquiry, Artist artist) {
        Artist owner = inquiry.getArtworkPackage().getArtwork().getArtist();
        if (!owner.getArtistId().equals(artist.getArtistId())) {
            throw new CustomException(ErrorCode.INQUIRY_ACCESS_DENIED);
        }
        return owner;
    }

    // 요청 슬롯이 이미 예약 확정된 다른 슬롯과 겹치면 거부(동시/중복 예약 방지).
    private void validateNotBooked(Artist artist, Inquiry inquiry) {
        int bookedMask = bookedSlotProvider.bookedMask(artist.getArtistId(), inquiry.getShootDate());
        int rangeMask = SlotGrid.rangeMask(
                inquiry.getStartTime(),
                inquiry.getStartTime().plusMinutes(inquiry.getDurationMinutesSnapshot()));
        if ((bookedMask & rangeMask) != 0) {
            throw new CustomException(ErrorCode.INQUIRY_SLOT_NOT_AVAILABLE);
        }
    }
}
