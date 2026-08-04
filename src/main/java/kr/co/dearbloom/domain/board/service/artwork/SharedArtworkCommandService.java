package kr.co.dearbloom.domain.board.service.artwork;

import kr.co.dearbloom.domain.board.entity.artwork.SharedArtwork;
import kr.co.dearbloom.domain.board.entity.board.SharedBoard;
import kr.co.dearbloom.domain.board.repository.artwork.SharedArtworkLikeRepository;
import kr.co.dearbloom.domain.board.repository.artwork.SharedArtworkRepository;
import kr.co.dearbloom.domain.customer.entity.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SharedArtworkCommandService {
    private final SharedArtworkRepository sharedArtworkRepository;
    private final SharedArtworkLikeRepository sharedArtworkLikeRepository;

    /**
     * 보드 삭제 시 그 보드의 공유 작품과 거기 달린 좋아요를 함께 정리한다(보드 삭제 경로에서 호출).
     * 좋아요 → 공유 작품 순서로 지워야 FK 제약에 걸리지 않는다.
     * 댓글은 보드 단위라 {@code SharedBoardCommentCommandService} 가 따로 정리한다.
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
