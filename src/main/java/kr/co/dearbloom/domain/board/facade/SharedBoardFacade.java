package kr.co.dearbloom.domain.board.facade;

import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.domain.artwork.service.ArtworkQueryService;
import kr.co.dearbloom.domain.board.dto.board.request.SharedBoardCreateRequest;
import kr.co.dearbloom.domain.board.dto.board.request.SharedBoardNameUpdateRequest;
import kr.co.dearbloom.domain.board.dto.board.response.SharedBoardResponse;
import kr.co.dearbloom.domain.board.dto.board.response.SharedBoardSummaryResponse;
import kr.co.dearbloom.domain.board.entity.board.SharedBoard;
import kr.co.dearbloom.domain.board.service.artwork.SharedArtworkCommandService;
import kr.co.dearbloom.domain.board.service.artwork.SharedArtworkQueryService;
import kr.co.dearbloom.domain.board.service.board.SharedBoardCommandService;
import kr.co.dearbloom.domain.board.service.board.SharedBoardQueryService;
import kr.co.dearbloom.domain.board.service.board.SharedCommentCommandService;
import kr.co.dearbloom.domain.board.service.board.SharedMemberCommandService;
import kr.co.dearbloom.domain.board.service.board.SharedMemberQueryService;
import kr.co.dearbloom.domain.customer.entity.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor
@Component
public class SharedBoardFacade {
    // 보드 카드에 노출할 미리보기 이미지 최대 장수.
    private static final int PREVIEW_IMAGE_LIMIT = 4;

    private final SharedBoardCommandService sharedBoardCommandService;
    private final SharedBoardQueryService sharedBoardQueryService;
    private final SharedMemberCommandService sharedMemberCommandService;
    private final SharedMemberQueryService sharedMemberQueryService;
    private final SharedCommentCommandService sharedCommentCommandService;
    private final SharedArtworkCommandService sharedArtworkCommandService;
    private final SharedArtworkQueryService sharedArtworkQueryService;
    private final ArtworkQueryService artworkQueryService;

    /**
     * 내가 참여 중인 공동보드 목록(보드 생성 오름차순).
     * 각 카드는 보드에 담긴 작품 수와 미리보기 이미지 최대 4장(작품별 첫 번째 사진, 담은 순서)으로 구성한다.
     */
    @Transactional(readOnly = true)
    public List<SharedBoardSummaryResponse> getJoinedBoards(Customer customer) {
        List<SharedBoard> sharedBoards = sharedMemberQueryService.getJoinedBoards(customer);
        if (sharedBoards.isEmpty()) {
            return List.of();
        }
        Map<Long, List<Artwork>> artworksByBoard = sharedArtworkQueryService.getArtworksByBoard(sharedBoards);
        Map<Long, String> representativeImages = artworkQueryService.getRepresentativeImageUrls(
                artworksByBoard.values().stream().flatMap(List::stream).toList());
        return sharedBoards.stream()
                .map(sharedBoard -> toSummary(sharedBoard,
                        artworksByBoard.getOrDefault(sharedBoard.getSharedBoardId(), List.of()),
                        representativeImages))
                .toList();
    }

    // 공동보드 생성. 생성한 고객이 방장이 되며, 방장도 참여자(SharedMember) 한 행으로 함께 들어간다.
    @Transactional
    public SharedBoardResponse create(Customer customer, SharedBoardCreateRequest request) {
        SharedBoard sharedBoard = sharedBoardCommandService.create(customer, request.getSharedBoardName());
        sharedMemberCommandService.join(sharedBoard, customer);
        return SharedBoardResponse.from(sharedBoard);
    }

    // 보드 이름 수정. 방장만 가능(방장이 아니면 403).
    @Transactional
    public SharedBoardResponse updateBoardName(Customer customer, Long sharedBoardId,
                                               SharedBoardNameUpdateRequest request) {
        SharedBoard sharedBoard = sharedBoardQueryService.getOwnedBy(sharedBoardId, customer);
        sharedBoardCommandService.updateBoardName(sharedBoard, request.getSharedBoardName());
        return SharedBoardResponse.from(sharedBoard);
    }

    /**
     * 공동보드 삭제. 방장만 가능(방장이 아니면 403).
     * 하위 데이터를 FK 역순으로 정리한다 — 보드 댓글 / 공유작품 좋아요 → 공유작품 → 공유멤버 → 보드.
     * 응답은 삭제 전 상태로 만들어 돌려준다.
     */
    @Transactional
    public SharedBoardResponse delete(Customer customer, Long sharedBoardId) {
        SharedBoard sharedBoard = sharedBoardQueryService.getOwnedBy(sharedBoardId, customer);
        SharedBoardResponse response = SharedBoardResponse.from(sharedBoard);
        sharedCommentCommandService.deleteBySharedBoard(sharedBoard);
        sharedArtworkCommandService.deleteBySharedBoard(sharedBoard);
        sharedMemberCommandService.deleteBySharedBoard(sharedBoard);
        sharedBoardCommandService.delete(sharedBoard);
        return response;
    }

    // 보드 카드 한 장으로 변환. 작품 수는 전체 기준, 이미지는 첫 번째 사진이 있는 작품에서 앞에서부터 최대 4장.
    private SharedBoardSummaryResponse toSummary(SharedBoard sharedBoard, List<Artwork> artworks,
                                                 Map<Long, String> representativeImages) {
        return new SharedBoardSummaryResponse(
                sharedBoard.getSharedBoardId(),
                sharedBoard.getBoardName(),
                artworks.size(),
                artworks.stream()
                        .map(artwork -> representativeImages.get(artwork.getArtworkId()))
                        .filter(Objects::nonNull)
                        .limit(PREVIEW_IMAGE_LIMIT)
                        .toList());
    }
}
