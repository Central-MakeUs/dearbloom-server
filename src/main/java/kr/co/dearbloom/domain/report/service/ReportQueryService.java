package kr.co.dearbloom.domain.report.service;

import kr.co.dearbloom.domain.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportQueryService {
    private final ReportRepository reportRepository;

    // 이 고객이 해당 작품을 신고했는지 여부.
    public boolean isArtworkReported(Long customerId, Long artworkId) {
        return reportRepository.existsByCustomer_CustomerIdAndArtwork_ArtworkId(customerId, artworkId);
    }
}
