package kr.co.dearbloom.domain.board.facade;

import kr.co.dearbloom.domain.artwork.dto.response.ArtworkSummaryResponse;
import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.domain.artwork.service.ArtworkQueryService;
import kr.co.dearbloom.domain.board.dto.artwork.request.SharedArtworkUpdateRequest;
import kr.co.dearbloom.domain.board.dto.artwork.response.SavedArtworkIsSharedResponse;
import kr.co.dearbloom.domain.board.dto.artwork.response.SharedArtworkItemResponse;
import kr.co.dearbloom.domain.board.dto.artwork.response.SharedArtworkPageResponse;
import kr.co.dearbloom.domain.board.dto.artwork.response.SharedArtworkSummaryResponse;
import kr.co.dearbloom.domain.board.dto.artwork.response.SharedArtworkUpdateResponse;
import kr.co.dearbloom.domain.board.dto.board.response.SharedMemberResponse;
import kr.co.dearbloom.domain.board.entity.artwork.SharedArtwork;
import kr.co.dearbloom.domain.board.entity.board.SharedBoard;
import kr.co.dearbloom.domain.board.service.artwork.SharedArtworkCommandService;
import kr.co.dearbloom.domain.board.service.artwork.SharedArtworkQueryService;
import kr.co.dearbloom.domain.board.service.board.SharedBoardQueryService;
import kr.co.dearbloom.domain.board.service.board.SharedMemberQueryService;
import kr.co.dearbloom.domain.customer.entity.Customer;
import kr.co.dearbloom.domain.customer.service.SavedArtworkQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RequiredArgsConstructor
@Component
public class SharedArtworkFacade {
    private final SharedBoardQueryService sharedBoardQueryService;
    private final SharedMemberQueryService sharedMemberQueryService;
    private final SharedArtworkCommandService sharedArtworkCommandService;
    private final SharedArtworkQueryService sharedArtworkQueryService;
    private final SavedArtworkQueryService savedArtworkQueryService;
    private final ArtworkQueryService artworkQueryService;

    /**
     * 공유작품 페이지(참여자 + 공유작품). 참여 중인 고객만 볼 수 있다(참여자가 아니면 403).
     * 같은 작품이 중복으로 담기지 않으므로 담긴 그대로 내려가며, 정렬은 좋아요 많은 순 → 먼저 담긴 순이다.
     */
    @Transactional(readOnly = true)
    public SharedArtworkPageResponse getPage(Customer customer, Long sharedBoardId) {
        SharedBoard sharedBoard = sharedBoardQueryService.getById(sharedBoardId);
        sharedMemberQueryService.getJoinedMember(sharedBoard, customer);
        List<SharedMemberResponse> members = sharedMemberQueryService.getMembers(sharedBoard).stream()
                .map(SharedMemberResponse::from)
                .toList();
        List<SharedArtworkSummaryResponse> sharedArtworks = toSummaries(
                sharedArtworkQueryService.getBySharedBoard(sharedBoard),
                sharedArtworkQueryService.getLikedSharedArtworkIds(sharedBoard, customer));
        return new SharedArtworkPageResponse(
                members.size(), members, sharedArtworks, sharedArtworks.size());
    }

    /**
     * 공유작품 고르기 화면용 내 저장 목록. 이 보드에 <b>내가 이미 공유한 작품이 맨 위</b>로 올라오고,
     * 나머지는 저장 목록 순서(저장 최신순)를 그대로 따른다. 참여자가 아니면 403.
     * <p>
     * 남이 이미 담은 작품은 내가 담을 수 없으므로, 프론트가 그 카드를 비활성화하고 담은 사람을
     * 보여줄 수 있도록 sharedBy 를 함께 내려준다(아무도 안 담았으면 null).
     */
    @Transactional(readOnly = true)
    public List<SavedArtworkIsSharedResponse> getSavedArtworks(Customer customer, Long sharedBoardId) {
        SharedBoard sharedBoard = sharedBoardQueryService.getById(sharedBoardId);
        sharedMemberQueryService.getJoinedMember(sharedBoard, customer);
        List<Artwork> savedArtworks = savedArtworkQueryService.getSavedArtworks(customer);
        if (savedArtworks.isEmpty()) {
            return List.of();
        }
        // 작품 → 담은 고객 → 그 고객의 공유멤버 정보. 멤버 목록은 한 번만 조회해 맵으로 돌려쓴다.
        Map<Long, Long> sharerCustomerIds = sharedArtworkQueryService.getSharerCustomerIdsByArtworkId(sharedBoard);
        Map<Long, SharedMemberResponse> membersByCustomerId = sharedMemberQueryService.getMembers(sharedBoard).stream()
                .collect(Collectors.toMap(
                        sharedMember -> sharedMember.getCustomer().getCustomerId(),
                        SharedMemberResponse::from));
        Set<Long> savedArtworkIds = savedArtworks.stream()
                .map(Artwork::getArtworkId)
                .collect(Collectors.toSet());
        return artworkQueryService.getSummaries(savedArtworks, savedArtworkIds).stream()
                .map(summary -> {
                    Long sharerCustomerId = sharerCustomerIds.get(summary.artworkId());
                    boolean isMine = customer.getCustomerId().equals(sharerCustomerId);
                    return new SavedArtworkIsSharedResponse(
                            summary,
                            isMine,
                            sharerCustomerId == null ? null : membersByCustomerId.get(sharerCustomerId));
                })
                // 내가 담은 작품을 앞으로. 동률은 stable sort 라 저장 목록 순서가 유지된다.
                .sorted(Comparator.comparing(SavedArtworkIsSharedResponse::isShared).reversed())
                .toList();
    }

    /**
     * 내 공유작품 업데이트(다중 선택). 보낸 작품 목록이 그대로 내 공유작품이 되며,
     * 빠진 작품은 거기 달린 좋아요와 함께 내려간다. 참여자가 아니면 403.
     * 같은 작품은 보드에 한 번만 담기므로, 다른 참여자가 이미 담은 작품이 목록에 있으면 409.
     */
    @Transactional
    public SharedArtworkUpdateResponse update(Customer customer, Long sharedBoardId,
                                              SharedArtworkUpdateRequest request) {
        SharedBoard sharedBoard = sharedBoardQueryService.getById(sharedBoardId);
        sharedMemberQueryService.getJoinedMember(sharedBoard, customer);
        List<Artwork> artworks = request.getArtworkIdList().stream()
                .distinct()
                .map(artworkQueryService::getById)
                .toList();
        sharedArtworkCommandService.replaceMine(sharedBoard, customer, artworks);
        return SharedArtworkUpdateResponse.of(sharedBoard,
                sharedArtworkQueryService.getMineWithArtwork(sharedBoard, customer).stream()
                        .map(SharedArtworkItemResponse::from)
                        .toList());
    }

    // 공유작품 좋아요 등록. 참여 중인 고객만 누를 수 있고(403), 이미 눌렀으면 409.
    @Transactional
    public void like(Customer customer, Long sharedArtworkId) {
        SharedArtwork sharedArtwork = sharedArtworkQueryService.getById(sharedArtworkId);
        sharedMemberQueryService.getJoinedMember(sharedArtwork.getSharedBoard(), customer);
        sharedArtworkCommandService.like(sharedArtwork, customer);
    }

    // 공유작품 좋아요 취소. 참여 중인 고객만 누를 수 있고(403), 누르지 않았어도 정상 처리(멱등).
    @Transactional
    public void unlike(Customer customer, Long sharedArtworkId) {
        SharedArtwork sharedArtwork = sharedArtworkQueryService.getById(sharedArtworkId);
        sharedMemberQueryService.getJoinedMember(sharedArtwork.getSharedBoard(), customer);
        sharedArtworkCommandService.unlike(sharedArtwork, customer);
    }

    /**
     * 공유작품 목록을 카드로 변환. 가격·대표 이미지는 작품 도메인의 배치 조회를 재사용하고
     * (넘긴 순서가 그대로 유지된다), 거기에 공유작품 ID 와 내 좋아요 여부만 얹는다.
     */
    private List<SharedArtworkSummaryResponse> toSummaries(List<SharedArtwork> sharedArtworks,
                                                           Set<Long> likedSharedArtworkIds) {
        if (sharedArtworks.isEmpty()) {
            return List.of();
        }
        List<ArtworkSummaryResponse> summaries = artworkQueryService.getSummaries(
                sharedArtworks.stream().map(SharedArtwork::getArtwork).toList(), null);
        return IntStream.range(0, summaries.size())
                .mapToObj(index -> {
                    ArtworkSummaryResponse summary = summaries.get(index);
                    Long sharedArtworkId = sharedArtworks.get(index).getSharedArtworkId();
                    return new SharedArtworkSummaryResponse(
                            sharedArtworkId,
                            summary.artworkId(),
                            summary.title(),
                            summary.lowestPrice(),
                            summary.minHeadCount(),
                            summary.maxHeadCount(),
                            summary.artistNickname(),
                            summary.artistRegionList(),
                            summary.thumbnailUrl(),
                            likedSharedArtworkIds.contains(sharedArtworkId));
                })
                .toList();
    }
}
