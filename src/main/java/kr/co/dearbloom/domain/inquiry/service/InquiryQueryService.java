package kr.co.dearbloom.domain.inquiry.service;

import kr.co.dearbloom.domain.artwork.entity.ArtworkPackage;
import kr.co.dearbloom.domain.artwork.repository.ArtworkPackageRepository;
import kr.co.dearbloom.domain.inquiry.entity.Inquiry;
import kr.co.dearbloom.domain.inquiry.entity.InquiryHistory;
import kr.co.dearbloom.domain.inquiry.repository.InquiryHistoryRepository;
import kr.co.dearbloom.domain.inquiry.repository.InquiryRepository;
import kr.co.dearbloom.global.dto.response.exception.CustomException;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 문의 관련 엔티티 조회. DTO 조립은 파사드가 담당. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryQueryService {
    private final ArtworkPackageRepository artworkPackageRepository;
    private final InquiryRepository inquiryRepository;
    private final InquiryHistoryRepository inquiryHistoryRepository;

    // 문의 대상 패키지 조회. 없으면 404.
    public ArtworkPackage getArtworkPackage(Long artworkPackageId) {
        return artworkPackageRepository.findById(artworkPackageId)
                .orElseThrow(() -> new CustomException(ErrorCode.ARTWORK_PACKAGE_NOT_FOUND));
    }

    // 문의 단건 조회. 없으면 404.
    public Inquiry getById(Long inquiryId) {
        return inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new CustomException(ErrorCode.INQUIRY_NOT_FOUND));
    }

    // 고객이 보낸 문의 리스트(최근 수정순).
    public List<Inquiry> getByCustomer(Long customerId) {
        return inquiryRepository.findByCustomerOrderByModifiedAtDesc(customerId);
    }

    // 작가 작품에 들어온 문의 리스트(촬영일 오름차순).
    public List<Inquiry> getByArtist(Long artistId) {
        return inquiryRepository.findByArtistOrderByShootDateAsc(artistId);
    }

    // 특정 문의의 상태 변경 이력(오래된 순).
    public List<InquiryHistory> getHistory(Long inquiryId) {
        return inquiryHistoryRepository.findByInquiry_InquiryIdOrderByCreatedAtAsc(inquiryId);
    }
}
