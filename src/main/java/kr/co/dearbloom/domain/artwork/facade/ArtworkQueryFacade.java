package kr.co.dearbloom.domain.artwork.facade;

import kr.co.dearbloom.domain.artist.entity.Artist;
import kr.co.dearbloom.domain.artwork.dto.response.ArtworkDetailResponse;
import kr.co.dearbloom.domain.artwork.dto.response.ArtworkThumbnailResponse;
import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.domain.artwork.entity.PortfolioFile;
import kr.co.dearbloom.domain.artwork.service.ArtworkQueryService;
import kr.co.dearbloom.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ArtworkQueryFacade {
    private final ArtworkQueryService artworkQueryService;

    /**
     * 작품 상세 조회. 뷰어에 따라 노출 정보가 다르다.
     * - 작가 본인: isMine=true, 저장 수 노출
     * - 비로그인 등 그 외: 기본 정보만
     * (고객 저장여부는 저장 기능 구현 후 추가 예정)
     */
    @Transactional(readOnly = true)
    public ArtworkDetailResponse getDetail(Long artworkId, Member member) {
        Artwork artwork = artworkQueryService.getById(artworkId);
        Artist artist = artwork.getArtist();
        List<PortfolioFile> files = artworkQueryService.getPortfolioFiles(artwork);
        List<ArtworkThumbnailResponse> otherArtworkList =
                artworkQueryService.getOtherArtworkThumbnails(artist, artworkId);
        return ArtworkDetailResponse.of(artwork, artist, files, otherArtworkList, isOwner(artwork, member));
    }

    // 로그인한 회원이 이 작품의 작가 본인인지 판별. 비로그인이면 false.
    private boolean isOwner(Artwork artwork, Member member) {
        return member != null
                && artwork.getArtist().getMember().getMemberId().equals(member.getMemberId());
    }
}
