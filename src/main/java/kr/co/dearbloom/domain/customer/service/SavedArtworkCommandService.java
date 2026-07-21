package kr.co.dearbloom.domain.customer.service;

import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.domain.customer.entity.Customer;
import kr.co.dearbloom.domain.customer.entity.SavedArtwork;
import kr.co.dearbloom.domain.customer.repository.SavedArtworkRepository;
import kr.co.dearbloom.global.dto.response.exception.CustomException;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SavedArtworkCommandService {
    private final SavedArtworkRepository savedArtworkRepository;

    // 작품 하나 저장. 이미 저장했으면 409.
    // TODO: MQ 도입 시 저장 이벤트를 발행해 artwork.savedCount 를 비동기 갱신.
    public void save(Customer customer, Artwork artwork) {
        if (savedArtworkRepository.existsByCustomerAndArtwork(customer, artwork)) {
            throw new CustomException(ErrorCode.ARTWORK_ALREADY_SAVED);
        }
        savedArtworkRepository.save(SavedArtwork.builder()
                .customer(customer)
                .artwork(artwork)
                .build());
    }

    // 단일 저장 취소. 저장돼 있지 않아도 조용히 통과(멱등).
    public void delete(Customer customer, Long artworkId) {
        savedArtworkRepository.deleteByCustomerAndArtwork_ArtworkId(customer, artworkId);
    }

    // 다중 저장 취소.
    public void deleteAll(Customer customer, List<Long> artworkIdList) {
        savedArtworkRepository.deleteByCustomerAndArtwork_ArtworkIdIn(customer, artworkIdList);
    }
}
