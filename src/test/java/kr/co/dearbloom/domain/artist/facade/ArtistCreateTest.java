package kr.co.dearbloom.domain.artist.facade;

import kr.co.dearbloom.domain.artist.dto.request.ArtistCreateRequest;
import kr.co.dearbloom.domain.artist.dto.response.ArtistCreateResponse;
import kr.co.dearbloom.domain.artist.entity.Region;
import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.domain.member.entity.MemberRole;
import kr.co.dearbloom.domain.member.repository.MemberRepository;
import kr.co.dearbloom.global.auth.jwt.TokenProvider;
import kr.co.dearbloom.global.dto.response.exception.CustomException;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 온보딩(POST /api/artists) 경로 검증. */
@SpringBootTest
class ArtistCreateTest {
    @Autowired ArtistFacade artistFacade;
    @Autowired MemberRepository memberRepository;
    @Autowired TokenProvider tokenProvider;

    private Member newMember() {
        return memberRepository.save(Member.builder()
                .name("온보딩테스트").email("onboard-" + System.nanoTime() + "@t.co").build());
    }

    private ArtistCreateRequest request(String nickname) {
        ArtistCreateRequest request = new ArtistCreateRequest();
        ReflectionTestUtils.setField(request, "nickname", nickname);
        ReflectionTestUtils.setField(request, "imageUrl",
                "https://cdn.dearbloom.co.kr/profile/artist/a.webp");
        ReflectionTestUtils.setField(request, "regions", Set.of(Region.SEOUL, Region.GYEONGGI));
        return request;
    }

    @Test
    void 작가_프로필을_생성하고_ARTIST_로_전환된_토큰을_반환한다() {
        Member member = newMember();

        ArtistCreateResponse response = artistFacade.create(member, request("온보딩작가"));

        assertThat(response.artist().nickname()).isEqualTo("온보딩작가");
        assertThat(response.artist().regions()).containsExactlyInAnyOrder("SEOUL", "GYEONGGI");
        // 새 토큰으로 이후 @CurrentArtist API 가 동작해야 하므로 두 클레임이 채워져야 한다
        assertThat(tokenProvider.getActiveRole(response.accessToken())).isEqualTo(MemberRole.ARTIST);
        assertThat(tokenProvider.getActiveProfileId(response.accessToken()))
                .isEqualTo(response.artist().artistId());
        assertThat(memberRepository.findById(member.getMemberId()).orElseThrow().isHasArtist()).isTrue();
    }

    @Test
    void 대표_이미지_없이도_생성된다() {
        Member member = newMember();
        ArtistCreateRequest request = request("이미지없는작가");
        ReflectionTestUtils.setField(request, "imageUrl", null);

        ArtistCreateResponse response = artistFacade.create(member, request);

        assertThat(response.artist().imageUrl()).isNull();
        assertThat(response.artist().regions()).containsExactlyInAnyOrder("SEOUL", "GYEONGGI");
    }

    @Test
    void 이미_작가_프로필이_있으면_409_를_낸다() {
        Member member = newMember();
        artistFacade.create(member, request("최초작가"));

        assertThatThrownBy(() -> artistFacade.create(member, request("중복작가")))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ARTIST_ALREADY_EXISTS.getCode());
    }
}
