package kr.co.dearbloom.domain.report.service;

import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.domain.customer.entity.Customer;
import kr.co.dearbloom.domain.report.entity.ArtworkReport;
import kr.co.dearbloom.domain.report.repository.ArtworkReportRepository;
import kr.co.dearbloom.global.dto.response.exception.CustomException;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ArtworkReportCommandService {
    private final ArtworkReportRepository artworkReportRepository;

    // 작품 신고 1건. 같은 작품을 이미 신고했으면 409.
    public void report(Customer customer, Artwork artwork, String content) {
        if (artworkReportRepository.existsByCustomerAndArtwork(customer, artwork)) {
            throw new CustomException(ErrorCode.ARTWORK_ALREADY_REPORTED);
        }
        artworkReportRepository.save(ArtworkReport.builder()
                .customer(customer)
                .artwork(artwork)
                .content(content)
                .build());
    }
}
