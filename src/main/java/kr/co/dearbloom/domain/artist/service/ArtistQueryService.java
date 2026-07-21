package kr.co.dearbloom.domain.artist.service;

import kr.co.dearbloom.domain.artist.entity.Artist;
import kr.co.dearbloom.domain.artist.repository.ArtistRepository;
import kr.co.dearbloom.global.dto.response.exception.CustomException;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class ArtistQueryService {
    private final ArtistRepository artistRepository;

    public Artist getWithRegions(Long artistId) {
        return artistRepository.findWithRegionsByArtistId(artistId)
                .orElseThrow(() -> new CustomException(ErrorCode.ARTIST_NOT_FOUND));
    }

    public boolean existsByNickname(String nickname) {
        return artistRepository.existsByNickname(nickname);
    }
}
