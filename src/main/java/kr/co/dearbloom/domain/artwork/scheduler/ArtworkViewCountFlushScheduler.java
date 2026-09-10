package kr.co.dearbloom.domain.artwork.scheduler;

import kr.co.dearbloom.domain.artwork.repository.ArtworkViewCountRepository;
import kr.co.dearbloom.domain.artwork.service.ArtworkViewCountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Redis 에 쌓인 조회수 증가분을 주기적으로 DB 로 옮긴다.
 *
 * <p><b>건수가 아니라 시간 기준이다.</b> "N 건마다 반영" 은 트래픽이 많아야 성립한다 —
 * 작품 하나가 N 건에 닿기까지 DB 의 조회수는 계속 0 이고, 그 사이 Redis 가 유일한 진실이 된다.
 * 시간 기준이면 조회가 드물어도 한 주기 안에 DB 에 남는다.
 *
 * <p><b>단일 인스턴스 전제.</b> 서버가 여러 대가 되면 같은 시각에 동시에 돌아 중복 반영될 수 있다.
 * 그때는 이 스케줄러에 잠금이 필요하다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ArtworkViewCountFlushScheduler {
    private final ArtworkViewCountRepository artworkViewCountRepository;
    private final ArtworkViewCountService artworkViewCountService;

    @Scheduled(fixedDelayString = "${artwork.view.flush-interval}")
    public void flush() {
        List<Long> artworkIds;
        try {
            artworkIds = artworkViewCountRepository.findDirtyArtworkIds();
        } catch (Exception e) {
            log.warn("[ViewCount] flush 대상 조회 실패 — {}", e.getMessage());
            return;
        }
        if (artworkIds.isEmpty()) {
            return;
        }

        int flushed = 0;
        for (Long artworkId : artworkIds) {
            try {
                if (artworkViewCountService.flushOne(artworkId)) {
                    flushed++;
                }
            } catch (Exception e) {
                // 증가분을 아직 빼지 않았으므로 다음 주기에 다시 시도된다.
                log.warn("[ViewCount] flush 실패 — artworkId={}, {}", artworkId, e.getMessage());
            }
        }
        log.info("[ViewCount] flush 완료 — 대상 {}건, 반영 {}건", artworkIds.size(), flushed);
    }
}
