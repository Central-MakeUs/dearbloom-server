package kr.co.dearbloom.domain.artist.facade;

import kr.co.dearbloom.domain.artist.dto.artist.response.ArtistResponse;
import kr.co.dearbloom.domain.artist.entity.artist.Artist;
import kr.co.dearbloom.domain.artist.entity.artist.Region;
import kr.co.dearbloom.domain.artist.repository.ArtistRepository;
import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * open-in-view: false 환경에서 @CurrentArtist 로 넘어오는 detached Artist 를 재현해
 * 응답 매핑이 LAZY 인 regions 를 읽어도 터지지 않는지 검증.
 */
@SpringBootTest
class ArtistFacadeLazyTest {
    @Autowired ArtistFacade artistFacade;
    @Autowired ArtistRepository artistRepository;
    @Autowired MemberRepository memberRepository;

    // 롤백 없이 실제 DB 에 남으므로 닉네임/이메일은 실행마다 유일해야 한다(닉네임 중복 검사에 걸림).
    private String uniqueNickname() {
        return "t" + (System.nanoTime() % 100_000_000L);
    }

    // 트랜잭션 밖에서 조회 → 리졸버가 주입하는 것과 같은 detached 상태
    private Artist detachedArtistWithRegions() {
        Member member = memberRepository.save(Member.builder()
                .name("t").email("lazy-" + System.nanoTime() + "@t.co").build());
        Artist saved = artistRepository.save(Artist.builder()
                .member(member)
                .nickname(uniqueNickname())
                .regions(Set.of(Region.SEOUL, Region.GYEONGGI_NORTH))
                .build());
        return artistRepository.findById(saved.getArtistId()).orElseThrow();
    }

    @Test
    void updateImage_detached_상태에서도_regions_를_응답에_담는다() {
        Artist detached = detachedArtistWithRegions();

        ArtistResponse response =
                artistFacade.updateImage(detached, "https://cdn.dearbloom.co.kr/a.webp");

        assertThat(response.regionList()).containsExactlyInAnyOrder("SEOUL", "GYEONGGI_NORTH");
    }

    @Test
    void updateNickname_detached_상태에서도_regions_를_응답에_담는다() {
        Artist detached = detachedArtistWithRegions();

        ArtistResponse response = artistFacade.updateNickname(detached, uniqueNickname());

        assertThat(response.regionList()).containsExactlyInAnyOrder("SEOUL", "GYEONGGI_NORTH");
    }
}
