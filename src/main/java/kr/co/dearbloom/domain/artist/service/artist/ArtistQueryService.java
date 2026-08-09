package kr.co.dearbloom.domain.artist.service.artist;

import kr.co.dearbloom.domain.artist.entity.artist.Artist;
import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.domain.artist.repository.ArtistRepository;
import kr.co.dearbloom.global.dto.response.exception.CustomException;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArtistQueryService {
    private final ArtistRepository artistRepository;

    public Artist getWithRegions(Long artistId) {
        return artistRepository.findWithRegionsByArtistId(artistId)
                .orElseThrow(() -> new CustomException(ErrorCode.ARTIST_NOT_FOUND));
    }

    public boolean existsByNickname(String nickname) {
        return artistRepository.existsByNickname(nickname);
    }

    // 이 멤버의 작가 프로필(없을 수 있음). 탈퇴 정리처럼 있으면 처리하고 없으면 넘어가는 경로용.
    public Optional<Artist> findByMember(Member member) {
        return artistRepository.findByMember(member);
    }
}
