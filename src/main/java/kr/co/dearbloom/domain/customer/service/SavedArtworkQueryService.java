package kr.co.dearbloom.domain.customer.service;

import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.domain.customer.entity.Customer;
import kr.co.dearbloom.domain.customer.repository.SavedArtworkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SavedArtworkQueryService {
    private final SavedArtworkRepository savedArtworkRepository;

    // 고객이 저장한 작품들을 저장 최신순으로 조회(작가 fetch join).
    public List<Artwork> getSavedArtworks(Customer customer) {
        return savedArtworkRepository.findSavedArtworksWithArtist(customer);
    }

    // 이 고객이 작품을 저장했는지 여부. (customerId = 토큰의 activeProfileId)
    public boolean isSaved(Long customerId, Long artworkId) {
        return savedArtworkRepository.existsByCustomer_CustomerIdAndArtwork_ArtworkId(customerId, artworkId);
    }

    // 이 고객이 저장한 작품 id 집합(리스트 저장 여부 일괄 판정용).
    public Set<Long> getSavedArtworkIds(Long customerId) {
        return savedArtworkRepository.findSavedArtworkIdsByCustomerId(customerId);
    }

    // 위와 같지만 지금 페이지에 뜬 작품들로만 좁혀 조회한다 — 페이지네이션 목록에서 저장 목록 전체를 끌어올 이유가 없다.
    public Set<Long> getSavedArtworkIds(Long customerId, Collection<Long> artworkIds) {
        if (artworkIds.isEmpty()) {
            return Set.of();
        }
        return savedArtworkRepository.findSavedArtworkIdsByCustomerIdAndArtworkIdIn(customerId, artworkIds);
    }
}
