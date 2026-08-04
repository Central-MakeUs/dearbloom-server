package kr.co.dearbloom.domain.board.service.artwork;

import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.domain.board.entity.artwork.SharedArtwork;
import kr.co.dearbloom.domain.board.entity.board.SharedBoard;
import kr.co.dearbloom.domain.board.repository.artwork.SharedArtworkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SharedArtworkQueryService {
    private final SharedArtworkRepository sharedArtworkRepository;

    /**
     * 보드 ID → 그 보드에 담긴 작품 목록(담은 순서) 맵. 한 번의 조회로 N+1 을 피한다.
     * 작품이 하나도 없는 보드는 키 자체가 없으므로 호출부에서 기본값 처리한다.
     */
    public Map<Long, List<Artwork>> getArtworksByBoard(Collection<SharedBoard> sharedBoards) {
        if (sharedBoards.isEmpty()) {
            return Map.of();
        }
        return sharedArtworkRepository.findBySharedBoardInWithArtwork(sharedBoards).stream()
                .collect(Collectors.groupingBy(
                        sharedArtwork -> sharedArtwork.getSharedBoard().getSharedBoardId(),
                        LinkedHashMap::new,
                        Collectors.mapping(SharedArtwork::getArtwork, Collectors.toList())));
    }
}
