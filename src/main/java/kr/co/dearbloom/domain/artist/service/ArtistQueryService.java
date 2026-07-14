package kr.co.dearbloom.domain.artist.service;

import kr.co.dearbloom.domain.artist.repository.ArtistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class ArtistQueryService {
    private final ArtistRepository artistRepository;

}
