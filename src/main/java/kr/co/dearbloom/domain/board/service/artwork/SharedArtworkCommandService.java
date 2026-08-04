package kr.co.dearbloom.domain.board.service.artwork;

import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.domain.board.entity.artwork.SharedArtwork;
import kr.co.dearbloom.domain.board.entity.artwork.SharedArtworkLike;
import kr.co.dearbloom.domain.board.entity.board.SharedBoard;
import kr.co.dearbloom.domain.board.repository.artwork.SharedArtworkLikeRepository;
import kr.co.dearbloom.domain.board.repository.artwork.SharedArtworkRepository;
import kr.co.dearbloom.domain.customer.entity.Customer;
import kr.co.dearbloom.global.dto.response.exception.CustomException;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SharedArtworkCommandService {
    private final SharedArtworkRepository sharedArtworkRepository;
    private final SharedArtworkLikeRepository sharedArtworkLikeRepository;

    /**
     * 내가 이 보드에 공유한 작품을 요청받은 목록으로 통째로 교체한다(부분 수정 아님).
     * 목록에서 빠진 내 공유작품은 거기 달린 좋아요와 함께 내려가고, 새로 들어온 작품만 추가된다.
     * 같은 작품을 다른 참여자가 이미 담았어도 참여자마다 한 행이므로 그대로 추가한다.
     */
    public void replaceMine(SharedBoard sharedBoard, Customer customer, List<Artwork> artworks) {
        List<SharedArtwork> mine = sharedArtworkRepository.findBySharedBoardAndCustomer(sharedBoard, customer);
        Set<Long> keepArtworkIds = artworks.stream().map(Artwork::getArtworkId).collect(Collectors.toSet());
        List<SharedArtwork> removed = mine.stream()
                .filter(sharedArtwork -> !keepArtworkIds.contains(sharedArtwork.getArtwork().getArtworkId()))
                .toList();
        if (!removed.isEmpty()) {
            sharedArtworkLikeRepository.deleteBySharedArtworkIn(removed);
            sharedArtworkRepository.deleteAll(removed);
        }
        Set<Long> alreadySharedArtworkIds = mine.stream()
                .map(sharedArtwork -> sharedArtwork.getArtwork().getArtworkId())
                .collect(Collectors.toSet());
        List<SharedArtwork> added = artworks.stream()
                .filter(artwork -> !alreadySharedArtworkIds.contains(artwork.getArtworkId()))
                .map(artwork -> SharedArtwork.builder()
                        .sharedBoard(sharedBoard)
                        .customer(customer)
                        .artwork(artwork)
                        .build())
                .toList();
        if (!added.isEmpty()) {
            sharedArtworkRepository.saveAll(added);
        }
    }

    // 공유작품 좋아요 등록. 이미 눌렀으면 409.
    public void like(SharedArtwork sharedArtwork, Customer customer) {
        if (sharedArtworkLikeRepository.existsBySharedArtworkAndCustomer(sharedArtwork, customer)) {
            throw new CustomException(ErrorCode.SHARED_ARTWORK_ALREADY_LIKED);
        }
        sharedArtworkLikeRepository.save(SharedArtworkLike.builder()
                .sharedArtwork(sharedArtwork)
                .customer(customer)
                .build());
    }

    // 공유작품 좋아요 취소. 누르지 않았어도 조용히 통과(멱등).
    public void unlike(SharedArtwork sharedArtwork, Customer customer) {
        sharedArtworkLikeRepository.deleteBySharedArtworkAndCustomer(sharedArtwork, customer);
    }

    /**
     * 보드 삭제 시 그 보드의 공유 작품과 거기 달린 좋아요를 함께 정리한다(보드 삭제 경로에서 호출).
     * 좋아요 → 공유 작품 순서로 지워야 FK 제약에 걸리지 않는다.
     * 댓글은 보드 단위라 {@code SharedCommentCommandService} 가 따로 정리한다.
     */
    public void deleteBySharedBoard(SharedBoard sharedBoard) {
        sharedArtworkLikeRepository.deleteBySharedArtwork_SharedBoard(sharedBoard);
        sharedArtworkRepository.deleteBySharedBoard(sharedBoard);
    }

    /**
     * 참여자 탈퇴 시 그가 이 보드에 담은 공유 작품과 누른 좋아요를 정리한다(탈퇴 경로에서 호출).
     * ① 그가 누른 좋아요(남의 작품에 누른 것 포함) → ② 그가 담은 작품에 달린 모든 좋아요(누른 사람 무관)
     * → ③ 그가 담은 공유 작품 순서로 지워야 FK 제약에 걸리지 않는다.
     * 원본 작품(Artwork)과 다른 참여자가 담은 공유 작품은 건드리지 않는다.
     */
    public void deleteBySharedBoardAndCustomer(SharedBoard sharedBoard, Customer customer) {
        sharedArtworkLikeRepository.deleteBySharedArtwork_SharedBoardAndCustomer(sharedBoard, customer);
        List<SharedArtwork> uploaded = sharedArtworkRepository.findBySharedBoardAndCustomer(sharedBoard, customer);
        if (uploaded.isEmpty()) {
            return;
        }
        sharedArtworkLikeRepository.deleteBySharedArtworkIn(uploaded);
        sharedArtworkRepository.deleteAll(uploaded);
    }
}
