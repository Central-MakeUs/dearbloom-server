package kr.co.dearbloom.domain.artist.service.artist;

import kr.co.dearbloom.domain.artist.dto.artist.request.ArtistCreateRequest;
import kr.co.dearbloom.domain.artist.entity.artist.Artist;
import kr.co.dearbloom.domain.artist.entity.artist.Region;
import kr.co.dearbloom.domain.artist.repository.ArtistRepository;
import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.global.dto.response.exception.CustomException;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class ArtistCommandService {
    private final ArtistRepository artistRepository;

    // 온보딩 3개 항목을 한 번에 받아 작가 프로필을 만든다. 한 Member 당 하나만 허용.
    // 과거 역할 해지로 익명화된 행이 남아 있으면(hasArtist=false) 그 행을 되살린다(재가입=같은 사람 복귀).
    // markAsArtist 이전에 호출되므로, 이미 활성 작가(hasArtist=true)가 다시 부르면 중복으로 막는다.
    public Artist create(Member member, ArtistCreateRequest request) {
        Artist existing = artistRepository.findByMember(member).orElse(null);
        if (existing != null) {
            if (member.isHasArtist()) {
                throw new CustomException(ErrorCode.ARTIST_ALREADY_EXISTS);
            }
            existing.reactivate(request.getNickname(), request.getImageUrl(), new HashSet<>(request.getRegionList()));
            return existing;
        }
        return artistRepository.save(Artist.builder()
                .member(member)
                .nickname(request.getNickname())
                .imageUrl(request.getImageUrl())
                .regions(new HashSet<>(request.getRegionList()))
                .build());
    }

    public Artist updateImage(Artist artist, String imageUrl) {
        artist.updateImageUrl(imageUrl);
        return artistRepository.save(artist);
        // @CurrentArtist 로 넘어온 엔티티는 detached 일 수 있어 명시 저장(merge)
    }

    public Artist updateNickname(Artist artist, String nickname) {
        artist.updateNickname(nickname);
        return artistRepository.save(artist);
    }

    public Artist updateIntro(Artist artist, String intro) {
        artist.updateIntro(intro);
        return artistRepository.save(artist);
    }

    public Artist updateRegions(Artist artist, Set<Region> regions) {
        // regions 가 LAZY 라 detached 상태에서 건드리면 터진다. merge 로 관리 상태를 먼저 확보.
        Artist managed = artistRepository.save(artist);
        managed.updateRegions(regions);
        return managed;
    }

    public Artist updateEtcInfo(Artist artist, String etcInfo) {
        artist.updateEtcInfo(etcInfo);
        return artistRepository.save(artist);
    }

    // 회원 탈퇴 시 이 멤버의 작가 프로필 익명화(있을 때만). regions(LAZY) 를 만지므로 managed 로드 후 처리.
    public void anonymizeByMember(Member member) {
        artistRepository.findByMember(member).ifPresent(Artist::anonymize);
    }
}
