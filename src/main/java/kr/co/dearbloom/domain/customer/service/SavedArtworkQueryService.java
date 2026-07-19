package kr.co.dearbloom.domain.customer.service;

import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.domain.customer.entity.Customer;
import kr.co.dearbloom.domain.customer.repository.SavedArtworkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SavedArtworkQueryService {
    private final SavedArtworkRepository savedArtworkRepository;

    // 고객이 저장한 작품들을 저장 최신순으로 조회(작가 fetch join).
    public List<Artwork> getSavedArtworks(Customer customer) {
        return savedArtworkRepository.findSavedArtworksWithArtist(customer);
    }
}
