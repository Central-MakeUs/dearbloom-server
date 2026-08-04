package kr.co.dearbloom.domain.board.service.artwork;

import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.domain.board.entity.artwork.SharedArtwork;
import kr.co.dearbloom.domain.board.entity.board.SharedBoard;
import kr.co.dearbloom.domain.board.repository.artwork.SharedArtworkLikeRepository;
import kr.co.dearbloom.domain.board.repository.artwork.SharedArtworkRepository;
import kr.co.dearbloom.domain.customer.entity.Customer;
import kr.co.dearbloom.global.dto.response.exception.CustomException;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SharedArtworkQueryService {
    private final SharedArtworkRepository sharedArtworkRepository;
    private final SharedArtworkLikeRepository sharedArtworkLikeRepository;

    public SharedArtwork getById(Long sharedArtworkId) {
        return sharedArtworkRepository.findById(sharedArtworkId)
                .orElseThrow(() -> new CustomException(ErrorCode.SHARED_ARTWORK_NOT_FOUND, sharedArtworkId));
    }

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

    /**
     * 보드의 공유작품을 <b>작품 단위로 중복 제거</b>해 화면 정렬대로 돌려준다.
     * 같은 작품을 여러 참여자가 담았으면 가장 먼저 담긴 행을 대표로 남긴다(응답의 공유작품 ID 도 그 행).
     * 정렬은 좋아요 많은 순, 같으면 먼저 담긴 순.
     */
    public List<SharedArtwork> getDistinctBySharedBoard(SharedBoard sharedBoard) {
        // 조회 쿼리가 담은 순으로 정렬돼 있어 먼저 만난 행이 곧 대표 행이다.
        List<SharedArtwork> representatives = sharedArtworkRepository.findBySharedBoardWithArtwork(sharedBoard)
                .stream()
                .collect(Collectors.toMap(
                        sharedArtwork -> sharedArtwork.getArtwork().getArtworkId(),
                        Function.identity(),
                        (first, second) -> first,
                        LinkedHashMap::new))
                .values().stream()
                .toList();
        Map<Long, Long> likeCounts = getLikeCounts(sharedBoard);
        return representatives.stream()
                // 정렬은 좋아요 내림차순만 지정 — 동률은 stable sort 라 담은 순(쿼리 순서)이 유지된다.
                .sorted(Comparator.comparingLong(
                        (SharedArtwork sharedArtwork) ->
                                likeCounts.getOrDefault(sharedArtwork.getArtwork().getArtworkId(), 0L))
                        .reversed())
                .toList();
    }

    // 내가 이 보드에 공유한 작품 id 집합(저장 목록에서 공유 여부 일괄 판정용).
    public Set<Long> getMySharedArtworkIds(SharedBoard sharedBoard, Customer customer) {
        return sharedArtworkRepository.findBySharedBoardAndCustomer(sharedBoard, customer).stream()
                .map(sharedArtwork -> sharedArtwork.getArtwork().getArtworkId())
                .collect(Collectors.toSet());
    }

    // 내가 이 보드에 공유 중인 작품(작품 fetch join). 업데이트 응답용.
    public List<SharedArtwork> getMineWithArtwork(SharedBoard sharedBoard, Customer customer) {
        return sharedArtworkRepository.findBySharedBoardAndCustomerWithArtwork(sharedBoard, customer);
    }

    // 이 보드에서 내가 좋아요한 작품 id 집합.
    public Set<Long> getLikedArtworkIds(SharedBoard sharedBoard, Customer customer) {
        return sharedArtworkLikeRepository.findLikedArtworkIds(sharedBoard, customer);
    }

    // 작품 id → 좋아요 수. 같은 작품의 여러 행에 달린 좋아요를 작품 단위로 합산한다.
    private Map<Long, Long> getLikeCounts(SharedBoard sharedBoard) {
        return sharedArtworkLikeRepository.countGroupedByArtwork(sharedBoard).stream()
                .collect(Collectors.toMap(
                        SharedArtworkLikeRepository.ArtworkLikeCount::getArtworkId,
                        SharedArtworkLikeRepository.ArtworkLikeCount::getLikeCount));
    }
}
