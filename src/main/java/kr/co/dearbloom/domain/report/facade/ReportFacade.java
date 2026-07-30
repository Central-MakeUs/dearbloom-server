package kr.co.dearbloom.domain.report.facade;

import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.domain.artwork.service.ArtworkQueryService;
import kr.co.dearbloom.domain.customer.entity.Customer;
import kr.co.dearbloom.domain.report.dto.request.ArtworkReportCreateRequest;
import kr.co.dearbloom.domain.report.dto.response.ArtworkReportedResponse;
import kr.co.dearbloom.domain.report.service.ReportCommandService;
import kr.co.dearbloom.domain.report.service.ReportQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** 고객 관점 신고 유스케이스. 대상별로 메서드를 두고 저장은 공용 Report 로 모인다. 처리(반려·처리완료)는 어드민 파사드에서 다룬다. */
@Component
@RequiredArgsConstructor
public class ReportFacade {
    private final ReportCommandService reportCommandService;
    private final ReportQueryService reportQueryService;
    private final ArtworkQueryService artworkQueryService;

    // 작품 신고. 존재하지 않는 작품이면 404, 이미 신고했으면 409.
    @Transactional
    public void reportArtwork(Customer customer, ArtworkReportCreateRequest request) {
        Artwork artwork = artworkQueryService.getById(request.getArtworkId());
        reportCommandService.reportArtwork(customer, artwork, request.getContent());
    }

    // 내가 이 작품을 신고했는지 여부. 작품이 없으면 404.
    @Transactional(readOnly = true)
    public ArtworkReportedResponse isArtworkReported(Customer customer, Long artworkId) {
        artworkQueryService.getById(artworkId);
        return ArtworkReportedResponse.of(
                artworkId,
                reportQueryService.isArtworkReported(customer.getCustomerId(), artworkId));
    }
}
