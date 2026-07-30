package kr.co.dearbloom.domain.report.service;

import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.domain.customer.entity.Customer;
import kr.co.dearbloom.domain.report.entity.Report;
import kr.co.dearbloom.domain.report.repository.ReportRepository;
import kr.co.dearbloom.global.dto.response.exception.CustomException;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportCommandService {
    private final ReportRepository reportRepository;

    // 작품 신고 1건. 같은 대상을 이미 신고했으면 409(nullable FK 라 unique 만으론 부족해 여기서 막는다).
    public void reportArtwork(Customer customer, Artwork artwork, String content) {
        if (reportRepository.existsByCustomerAndArtwork(customer, artwork)) {
            throw new CustomException(ErrorCode.ALREADY_REPORTED);
        }
        reportRepository.save(Report.ofArtwork(customer, artwork, content));
    }
}
