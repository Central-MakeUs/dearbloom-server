package kr.co.dearbloom.domain.report.facade;

import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.domain.artwork.service.ArtworkQueryService;
import kr.co.dearbloom.domain.customer.entity.Customer;
import kr.co.dearbloom.domain.report.dto.request.ArtworkReportCreateRequest;
import kr.co.dearbloom.domain.report.dto.response.ArtworkReportedResponse;
import kr.co.dearbloom.domain.report.service.ArtworkReportCommandService;
import kr.co.dearbloom.domain.report.service.ArtworkReportQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** 고객 관점 작품 신고 유스케이스. 처리(반려·블라인드)는 어드민 파사드에서 다룬다. */
@Component
@RequiredArgsConstructor
public class ArtworkReportFacade {
    private final ArtworkReportCommandService artworkReportCommandService;
    private final ArtworkReportQueryService artworkReportQueryService;
    private final ArtworkQueryService artworkQueryService;

    // 작품 신고. 존재하지 않는 작품이면 404, 이미 신고했으면 409.
    @Transactional
    public void report(Customer customer, ArtworkReportCreateRequest request) {
        Artwork artwork = artworkQueryService.getById(request.getArtworkId());
        artworkReportCommandService.report(customer, artwork, request.getContent());
    }

    // 내가 이 작품을 신고했는지 여부. 작품이 없으면 404.
    @Transactional(readOnly = true)
    public ArtworkReportedResponse isReported(Customer customer, Long artworkId) {
        artworkQueryService.getById(artworkId);
        return ArtworkReportedResponse.of(
                artworkId,
                artworkReportQueryService.isReported(customer.getCustomerId(), artworkId));
    }
}
