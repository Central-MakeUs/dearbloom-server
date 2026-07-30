package kr.co.dearbloom.domain.report.facade;

import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.domain.artwork.service.ArtworkQueryService;
import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.domain.report.dto.request.ArtworkReportCreateRequest;
import kr.co.dearbloom.domain.report.dto.response.ArtworkReportedResponse;
import kr.co.dearbloom.domain.report.service.ReportCommandService;
import kr.co.dearbloom.domain.report.service.ReportQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 신고 유스케이스. 신고자는 Member 로 잡고, "어떤 역할로 신고했는지"는 대상에서 유도된다.
 * 방향 제약(고객→작가 / 작가→고객)은 역할별 엔드포인트 분리로 지킨다 — 작품 신고는 고객 전용 경로에만 있다.
 * 처리(반려·처리완료)는 어드민 파사드에서 다룬다.
 */
@Component
@RequiredArgsConstructor
public class ReportFacade {
    private final ReportCommandService reportCommandService;
    private final ReportQueryService reportQueryService;
    private final ArtworkQueryService artworkQueryService;

    // 작품 신고. 존재하지 않는 작품이면 404, 이미 신고했으면 409.
    @Transactional
    public void reportArtwork(Member reporter, ArtworkReportCreateRequest request) {
        Artwork artwork = artworkQueryService.getById(request.getArtworkId());
        reportCommandService.reportArtwork(reporter, artwork, request.getContent());
    }

    // 내가 이 작품을 신고했는지 여부. 작품이 없으면 404.
    @Transactional(readOnly = true)
    public ArtworkReportedResponse isArtworkReported(Member reporter, Long artworkId) {
        artworkQueryService.getById(artworkId);
        return ArtworkReportedResponse.of(
                artworkId,
                reportQueryService.isArtworkReported(reporter.getMemberId(), artworkId));
    }
}
