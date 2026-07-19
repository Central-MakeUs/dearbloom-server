package kr.co.dearbloom.domain.customer.facade;

import kr.co.dearbloom.domain.artwork.dto.response.ArtworkSummaryResponse;
import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.domain.artwork.service.ArtworkQueryService;
import kr.co.dearbloom.domain.customer.dto.request.SavedArtworkDeleteRequest;
import kr.co.dearbloom.domain.customer.entity.Customer;
import kr.co.dearbloom.domain.customer.service.SavedArtworkCommandService;
import kr.co.dearbloom.domain.customer.service.SavedArtworkQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SavedArtworkFacade {
    private final SavedArtworkCommandService savedArtworkCommandService;
    private final SavedArtworkQueryService savedArtworkQueryService;
    private final ArtworkQueryService artworkQueryService;

    // 작품 하나 저장. 존재하지 않는 작품이면 404.
    @Transactional
    public void save(Customer customer, Long artworkId) {
        Artwork artwork = artworkQueryService.getById(artworkId);
        savedArtworkCommandService.save(customer, artwork);
    }

    // 내 저장 목록(저장 최신순).
    @Transactional(readOnly = true)
    public List<ArtworkSummaryResponse> getSavedList(Customer customer) {
        List<Artwork> artworks = savedArtworkQueryService.getSavedArtworks(customer);
        return artworkQueryService.getSummaries(artworks);
    }

    @Transactional
    public void delete(Customer customer, Long artworkId) {
        savedArtworkCommandService.delete(customer, artworkId);
    }

    @Transactional
    public void deleteAll(Customer customer, SavedArtworkDeleteRequest request) {
        savedArtworkCommandService.deleteAll(customer, request.getArtworkIdList());
    }

}
