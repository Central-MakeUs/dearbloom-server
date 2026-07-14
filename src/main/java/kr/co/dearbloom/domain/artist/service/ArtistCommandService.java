package kr.co.dearbloom.domain.artist.service;

import kr.co.dearbloom.domain.artist.entity.Artist;
import kr.co.dearbloom.domain.artist.repository.ArtistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
@RequiredArgsConstructor
public class ArtistCommandService {
    private final ArtistRepository artistRepository;

    public Artist updateProfileImage(Artist artist, String profileImageUrl) {
        artist.updateProfileImageUrl(profileImageUrl);
        return artistRepository.save(artist);
        // @CurrentArtist 로 넘어온 엔티티는 detached 일 수 있어 명시 저장(merge)
    }
}
