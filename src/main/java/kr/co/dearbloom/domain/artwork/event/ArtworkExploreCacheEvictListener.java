package kr.co.dearbloom.domain.artwork.event;

import kr.co.dearbloom.domain.artwork.repository.ArtworkExploreCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 작품 탐색 첫 화면 캐시 무효화.
 *
 * <p><b>{@code AFTER_COMMIT} 이어야 한다.</b> 트랜잭션 안에서 지우면 이런 순서가 가능하다 —
 * <ol>
 *   <li>트랜잭션이 캐시를 지운다</li>
 *   <li>다른 요청이 미스 → DB 조회 → <b>아직 커밋 안 된 변경을 못 본다</b> → 옛 목록을 캐시에 쓴다</li>
 *   <li>트랜잭션이 커밋된다</li>
 *   <li>캐시에 옛 목록이 남고, <b>이걸 고칠 이벤트는 더 이상 없다</b> → TTL 까지 낡은 채로 남는다</li>
 * </ol>
 *
 * <p><b>{@code @Async} 를 붙이지 않는다.</b> 푸시 리스너와 달리 여기서 하는 일은 키 하나 삭제라
 * 즉시 끝난다. 비동기로 미루면 커밋과 삭제 사이 간격만 넓어져 그동안 낡은 목록이 나간다.
 *
 * <p>삭제(evict)이지 갱신(refresh)이 아닌 이유 — 프론트에서 작품 등록 한 번이 API 3~4 번이다
 * (작품 생성 → 사진 교체 → 패키지 교체). 갱신 방식이면 한 번의 등록에 목록을 그만큼 다시 계산한다.
 * 삭제는 멱등하고 공짜이며, 다음 조회가 한 번만 채운다.
 */
@Component
@RequiredArgsConstructor
public class ArtworkExploreCacheEvictListener {
    private final ArtworkExploreCacheRepository artworkExploreCacheRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onArtworkExploreChanged(ArtworkExploreChangedEvent event) {
        artworkExploreCacheRepository.evict();
    }
}
