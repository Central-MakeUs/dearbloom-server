package kr.co.dearbloom.domain.inquiry.facade;

import kr.co.dearbloom.domain.artist.dto.schedule.response.DayAvailabilityResponse;
import kr.co.dearbloom.domain.artist.entity.artist.Artist;
import kr.co.dearbloom.domain.artist.service.schedule.ScheduleAvailabilityService;
import kr.co.dearbloom.domain.artist.util.BookingWindow;
import kr.co.dearbloom.domain.artist.util.SlotGrid;
import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.domain.artwork.entity.ArtworkPackage;
import kr.co.dearbloom.domain.artwork.service.ArtworkQueryService;
import kr.co.dearbloom.domain.customer.entity.Customer;
import kr.co.dearbloom.domain.inquiry.dto.request.InquiryCreateRequest;
import kr.co.dearbloom.domain.inquiry.dto.response.InquiryCreateResponse;
import kr.co.dearbloom.domain.inquiry.dto.response.InquiryStatusResponse;
import kr.co.dearbloom.domain.inquiry.dto.response.customer.CustomerInquiryDetailResponse;
import kr.co.dearbloom.domain.inquiry.dto.response.customer.CustomerInquiryListItemResponse;
import kr.co.dearbloom.domain.inquiry.dto.response.customer.InquiryPreparationResponse;
import kr.co.dearbloom.domain.inquiry.entity.inquiry.Inquiry;
import kr.co.dearbloom.domain.inquiry.entity.inquiry.InquiryStatus;
import kr.co.dearbloom.domain.inquiry.service.InquiryCommandService;
import kr.co.dearbloom.domain.inquiry.service.InquiryHistoryCommandService;
import kr.co.dearbloom.domain.inquiry.service.InquiryQueryService;
import kr.co.dearbloom.domain.member.entity.MemberRole;
import kr.co.dearbloom.domain.university.entity.University;
import kr.co.dearbloom.domain.university.service.UniversityQueryService;
import kr.co.dearbloom.global.dto.response.exception.CustomException;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/** 고객 관점 문의 유스케이스 (준비/전송/취소/리스트/상세). */
@Component
@RequiredArgsConstructor
public class CustomerInquiryFacade {
    private final InquiryQueryService inquiryQueryService;
    private final InquiryCommandService inquiryCommandService;
    private final InquiryHistoryCommandService inquiryHistoryCommandService;
    private final ArtworkQueryService artworkQueryService;
    private final UniversityQueryService universityQueryService;
    private final ScheduleAvailabilityService scheduleAvailabilityService;

    /**
     * 스마트 문의 준비 정보 조립. 패키지→작품→작가 LAZY 연관을 이 트랜잭션 안에서 초기화하며 DTO 로 매핑한다.
     * (open-in-view=false 라 매핑을 트랜잭션 안에서 끝내야 함)
     */
    @Transactional(readOnly = true)
    public InquiryPreparationResponse getPreparation(Long artworkPackageId) {
        ArtworkPackage artworkPackage = inquiryQueryService.getArtworkPackage(artworkPackageId);
        Artwork artwork = artworkPackage.getArtwork();
        Artist artist = artwork.getArtist();

        List<DayAvailabilityResponse> availability = scheduleAvailabilityService.getCalendar(
                artist, BookingWindow.firstOpenDate(), BookingWindow.lastOpenDate());

        return InquiryPreparationResponse.of(
                artwork, artist, artworkPackage, artworkQueryService.getRepresentativeImageUrl(artwork), availability);
    }

    /**
     * 스마트 문의 전송. 학교(대학 선택/자유입력) 해석 → 슬롯·인원 검증 → 스냅샷과 함께 저장.
     * 슬롯 잠금은 하지 않는다(문의=제안).
     */
    @Transactional
    public InquiryCreateResponse createInquiry(Customer customer, InquiryCreateRequest request) {
        ArtworkPackage artworkPackage = inquiryQueryService.getArtworkPackage(request.getArtworkPackageId());
        Artwork artwork = artworkPackage.getArtwork();
        Artist artist = artwork.getArtist();

        University university = resolveUniversity(request);
        String schoolName = resolveSchoolName(request, university);

        validateHeadCount(artwork, request.getHeadCount());
        validateSlot(artist, request.getShootDate(), request.getStartTime(), artworkPackage.getDurationMinutes());

        Inquiry inquiry = inquiryCommandService.create(
                customer, artworkPackage, university, schoolName,
                request.getShootDate(), request.getStartTime(), request.getHeadCount(), request.getRequestNote());
        // 생성 이력(null → IN_PROGRESS, 고객).
        inquiryHistoryCommandService.record(inquiry, null, MemberRole.CUSTOMER);
        return InquiryCreateResponse.from(inquiry);
    }

    /** 문의 취소(고객). 본인 문의 + 진행중일 때만. */
    @Transactional
    public InquiryStatusResponse cancelInquiry(Customer customer, Long inquiryId) {
        Inquiry inquiry = inquiryQueryService.getById(inquiryId);
        if (!inquiry.getCustomer().getCustomerId().equals(customer.getCustomerId())) {
            throw new CustomException(ErrorCode.INQUIRY_ACCESS_DENIED);
        }
        InquiryStatus from = inquiry.getStatus();
        inquiry.cancelAsInquiry();
        inquiryHistoryCommandService.record(inquiry, from, MemberRole.CUSTOMER);
        return InquiryStatusResponse.of(inquiry);
    }

    /** 고객이 보낸 문의 리스트(최근 수정순). 작품 대표 이미지는 배치로 조회한다. */
    @Transactional(readOnly = true)
    public List<CustomerInquiryListItemResponse> getMyInquiries(Customer customer) {
        List<Inquiry> inquiries = inquiryQueryService.getByCustomer(customer.getCustomerId());
        Map<Long, String> imageByArtworkId = artworkQueryService.getRepresentativeImageUrls(
                inquiries.stream().map(inquiry -> inquiry.getArtworkPackage().getArtwork()).toList());
        return inquiries.stream()
                .map(inquiry -> CustomerInquiryListItemResponse.of(
                        inquiry, imageByArtworkId.get(inquiry.getArtworkPackage().getArtwork().getArtworkId())))
                .toList();
    }

    /** 고객 문의 상세. 본인 문의만. */
    @Transactional(readOnly = true)
    public CustomerInquiryDetailResponse getMyInquiryDetail(Customer customer, Long inquiryId) {
        Inquiry inquiry = inquiryQueryService.getById(inquiryId);
        if (!inquiry.getCustomer().getCustomerId().equals(customer.getCustomerId())) {
            throw new CustomException(ErrorCode.INQUIRY_ACCESS_DENIED);
        }
        String imageUrl = artworkQueryService.getRepresentativeImageUrl(inquiry.getArtworkPackage().getArtwork());
        return CustomerInquiryDetailResponse.of(inquiry, imageUrl);
    }

    // 대학 목록에서 골랐으면 University 조회(없으면 404), 자유입력이면 null.
    private University resolveUniversity(InquiryCreateRequest request) {
        return request.getUniversityId() == null
                ? null
                : universityQueryService.findById(request.getUniversityId());
    }

    // 표시/스냅샷용 학교명. 대학 선택 시 그 이름, 자유입력 시 입력값. 둘 다 없으면 400.
    private String resolveSchoolName(InquiryCreateRequest request, University university) {
        if (university != null) {
            return university.getName();
        }
        if (request.getSchoolName() != null && !request.getSchoolName().isBlank()) {
            return request.getSchoolName().trim();
        }
        throw new CustomException(ErrorCode.INQUIRY_SCHOOL_REQUIRED);
    }

    // 촬영 인원이 작품의 min~max 범위 안인지. max null 이면 상한 없음.
    private void validateHeadCount(Artwork artwork, int headCount) {
        Integer min = artwork.getMinHeadCount();
        Integer max = artwork.getMaxHeadCount();
        if ((min != null && headCount < min) || (max != null && headCount > max)) {
            throw new CustomException(ErrorCode.INQUIRY_INVALID_HEAD_COUNT);
        }
    }

    // [startTime, startTime+duration) 가 그 날짜의 가용 셀에 전부 들어가는지 재검증(창밖/과거/블록/예약 반영).
    private void validateSlot(Artist artist, LocalDate date, LocalTime startTime, Integer durationMinutes) {
        if (durationMinutes == null) {
            throw new CustomException(ErrorCode.INQUIRY_SLOT_NOT_AVAILABLE);
        }
        int startIndex = SlotGrid.toStartIndex(startTime);
        int need = SlotGrid.requiredSlots(durationMinutes);
        int availableMask = scheduleAvailabilityService.availableMask(artist, date);
        if ((SlotGrid.startableMask(availableMask, need) & (1 << startIndex)) == 0) {
            throw new CustomException(ErrorCode.INQUIRY_SLOT_NOT_AVAILABLE);
        }
    }
}
