package kr.co.dearbloom.domain.artwork.service;

import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.domain.artwork.repository.ArtworkRepository;
import kr.co.dearbloom.domain.artwork.repository.ArtworkViewCountRepository;
import kr.co.dearbloom.global.auth.resolver.ViewerContext;
import kr.co.dearbloom.global.properties.ArtworkViewProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 작품 조회수 집계. 세는 규칙과 DB 반영을 함께 맡는다.
 *
 * <p><b>세는 조건 세 가지.</b> 셋 중 하나라도 어긋나면 세지 않는다.
 * <ol>
 *   <li><b>로그인한 회원이어야 한다.</b> 비로그인은 식별자가 없어 중복을 가릴 수 없다
 *       (IP 로 가르는 방법이 있지만 개인정보라 쓰지 않는다). 그래서 이 값은 "회원 조회수"다</li>
 *   <li><b>작가 본인은 제외한다.</b> 등록·수정 직후 본인이 확인하는 횟수가 적지 않아,
 *       조회수가 낮은 초기에 지표를 통째로 왜곡한다</li>
 *   <li><b>TTL 안의 재조회는 무시한다.</b> 새로고침·뒤로가기로 부풀지 않게 한다</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArtworkViewCountService {
    private final ArtworkViewCountRepository artworkViewCountRepository;
    private final ArtworkRepository artworkRepository;
    private final ArtworkViewProperties properties;

    /** 조회 1건 기록. 셀 대상이 아니면 조용히 넘어간다. */
    public void record(Artwork artwork, ViewerContext viewer) {
        if (viewer.member() == null || isOwnArtwork(artwork, viewer)) {
            return;
        }
        Long artworkId = artwork.getArtworkId();
        if (artworkViewCountRepository.markFirstView(
                artworkId, viewer.member().getMemberId(), properties.dedupTtlOrDefault())) {
            artworkViewCountRepository.increase(artworkId);
        }
    }

    /**
     * 쌓인 증가분 하나를 DB 에 더한다. 작품 하나당 트랜잭션 하나다 —
     * 한 건이 실패해도 나머지 작품의 반영까지 되돌아가면 안 된다.
     *
     * @return 반영했으면 true, 작품이 이미 삭제됐으면 false
     */
    @Transactional
    public boolean flushOne(Long artworkId) {
        long delta = artworkViewCountRepository.readDelta(artworkId);
        if (delta <= 0) {
            artworkViewCountRepository.clear(artworkId);
            return true;
        }
        if (artworkRepository.increaseViewCount(artworkId, delta) == 0) {
            log.info("[ViewCount] 삭제된 작품의 집계를 버린다 — artworkId={}, delta={}", artworkId, delta);
            artworkViewCountRepository.clear(artworkId);
            return false;
        }
        artworkViewCountRepository.subtractApplied(artworkId, delta);
        return true;
    }

    private boolean isOwnArtwork(Artwork artwork, ViewerContext viewer) {
        return viewer.isArtist()
                && viewer.activeProfileId().equals(artwork.getArtist().getArtistId());
    }
}
