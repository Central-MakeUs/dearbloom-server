package kr.co.dearbloom.domain.artwork.service;

import kr.co.dearbloom.domain.artist.entity.artist.Artist;
import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.domain.artwork.entity.PortfolioFile;
import kr.co.dearbloom.domain.artwork.repository.ArtworkRepository;
import kr.co.dearbloom.domain.artwork.repository.PortfolioFileRepository;
import kr.co.dearbloom.domain.customer.service.SavedArtworkCommandService;
import kr.co.dearbloom.domain.inquiry.service.InquiryCommandService;
import kr.co.dearbloom.domain.report.service.ReportCommandService;
import kr.co.dearbloom.global.file.FileCleaner;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 작가 탈퇴 시 그 작가의 작품을 전부 지운다.
 * <p>
 * 작품을 남기면 탈퇴한 작가의 작품이 목록에 계속 노출되고 문의까지 들어온다.
 * 사진에는 얼굴이 담겨 있어 <b>DB 행뿐 아니라 S3 객체까지</b> 지워야 한다 —
 * CDN URL 만 알면 영구히 열람 가능하기 때문.
 * <p>
 * 작품을 참조하는 다른 도메인 행을 먼저 정리해야 FK 제약에 걸리지 않는다.
 * 저장(SavedArtwork)·신고(Report)는 대상이 사라지면 의미가 없어 삭제하고,
 * 문의(Inquiry)는 상대방의 거래 이력이라 스냅샷만 남기고 참조를 끊는다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ArtworkWithdrawalService {
    private final ArtworkRepository artworkRepository;
    private final PortfolioFileRepository portfolioFileRepository;
    private final ArtworkCommandService artworkCommandService;
    private final SavedArtworkCommandService savedArtworkCommandService;
    private final InquiryCommandService inquiryCommandService;
    private final ReportCommandService reportCommandService;
    private final FileCleaner fileCleaner;

    public void deleteAllByArtist(Artist artist) {
        List<Artwork> artworks = artworkRepository.findByArtist(artist);
        if (artworks.isEmpty()) {
            return;
        }
        // 행을 지우면 URL 을 못 찾으므로 S3 삭제 대상을 먼저 모아둔다.
        List<String> photoUrls = portfolioFileRepository.findByArtworkInOrderBySortOrderAsc(artworks).stream()
                .map(PortfolioFile::getFileUrl)
                .toList();

        for (Artwork artwork : artworks) {
            savedArtworkCommandService.deleteByArtwork(artwork);
            reportCommandService.deleteByArtwork(artwork);
            inquiryCommandService.detachArtwork(artwork);
            artworkCommandService.delete(artwork);
        }
        fileCleaner.deleteAllQuietly(photoUrls);
    }
}
