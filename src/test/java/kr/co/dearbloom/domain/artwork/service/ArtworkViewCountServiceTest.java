package kr.co.dearbloom.domain.artwork.service;

import kr.co.dearbloom.domain.artist.entity.artist.Artist;
import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.domain.artwork.repository.ArtworkRepository;
import kr.co.dearbloom.domain.artwork.repository.ArtworkViewCountRepository;
import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.domain.member.entity.MemberRole;
import kr.co.dearbloom.global.auth.resolver.ViewerContext;
import kr.co.dearbloom.global.properties.ArtworkViewProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/** 조회수를 "세는 조건" 검증. 부풀거나 왜곡되지 않는 게 이 값의 존재 이유다. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ArtworkViewCountServiceTest {
    private static final Long ARTWORK_ID = 10L;
    private static final Long OWNER_ARTIST_ID = 7L;

    @Mock
    private ArtworkViewCountRepository artworkViewCountRepository;
    @Mock
    private ArtworkRepository artworkRepository;

    private ArtworkViewCountService service;
    private Artwork artwork;

    @BeforeEach
    void setUp() {
        service = new ArtworkViewCountService(
                artworkViewCountRepository, artworkRepository,
                new ArtworkViewProperties(Duration.ofHours(1)));

        Artist owner = Artist.builder().build();
        ReflectionTestUtils.setField(owner, "artistId", OWNER_ARTIST_ID);
        artwork = Artwork.builder().artist(owner).build();
        ReflectionTestUtils.setField(artwork, "artworkId", ARTWORK_ID);
    }

    private ViewerContext loggedIn(Long memberId, MemberRole role, Long profileId) {
        Member member = Member.builder().build();
        ReflectionTestUtils.setField(member, "memberId", memberId);
        return new ViewerContext(member, role, profileId);
    }

    @Test
    @DisplayName("비로그인은 세지 않는다 — 식별자가 없어 중복을 가릴 수 없다")
    void skipsGuest() {
        service.record(artwork, ViewerContext.guest());

        verify(artworkViewCountRepository, never()).markFirstView(anyLong(), anyLong(), any());
        verify(artworkViewCountRepository, never()).increase(anyLong());
    }

    @Test
    @DisplayName("작가 본인 조회는 세지 않는다 — 초기 조회수를 통째로 왜곡한다")
    void skipsOwnArtist() {
        ViewerContext owner = loggedIn(1L, MemberRole.ARTIST, OWNER_ARTIST_ID);

        service.record(artwork, owner);

        verify(artworkViewCountRepository, never()).markFirstView(anyLong(), anyLong(), any());
    }

    @Test
    @DisplayName("다른 작가가 보면 센다 — 제외 대상은 그 작품의 주인뿐이다")
    void countsOtherArtist() {
        ViewerContext other = loggedIn(2L, MemberRole.ARTIST, 99L);
        given(artworkViewCountRepository.markFirstView(eq(ARTWORK_ID), eq(2L), any())).willReturn(true);

        service.record(artwork, other);

        verify(artworkViewCountRepository).increase(ARTWORK_ID);
    }

    @Test
    @DisplayName("첫 조회면 증가분을 올린다")
    void countsFirstView() {
        ViewerContext customer = loggedIn(3L, MemberRole.CUSTOMER, 42L);
        given(artworkViewCountRepository.markFirstView(eq(ARTWORK_ID), eq(3L), eq(Duration.ofHours(1))))
                .willReturn(true);

        service.record(artwork, customer);

        verify(artworkViewCountRepository).increase(ARTWORK_ID);
    }

    @Test
    @DisplayName("TTL 안의 재조회는 증가분을 올리지 않는다")
    void skipsRepeatedViewWithinTtl() {
        ViewerContext customer = loggedIn(3L, MemberRole.CUSTOMER, 42L);
        given(artworkViewCountRepository.markFirstView(anyLong(), anyLong(), any())).willReturn(false);

        service.record(artwork, customer);

        verify(artworkViewCountRepository, never()).increase(anyLong());
    }

    @Test
    @DisplayName("flush 는 DB 에 반영한 만큼만 증가분에서 뺀다 — 그 사이 들어온 조회가 사라지면 안 된다")
    void subtractsOnlyWhatWasApplied() {
        given(artworkViewCountRepository.readDelta(ARTWORK_ID)).willReturn(5L);
        given(artworkRepository.increaseViewCount(ARTWORK_ID, 5L)).willReturn(1);

        assertThat(service.flushOne(ARTWORK_ID)).isTrue();

        verify(artworkViewCountRepository).subtractApplied(ARTWORK_ID, 5L);
    }

    @Test
    @DisplayName("이미 삭제된 작품이면 증가분을 버린다 — DB 에 더할 곳이 없다")
    void discardsDeletedArtwork() {
        given(artworkViewCountRepository.readDelta(ARTWORK_ID)).willReturn(3L);
        given(artworkRepository.increaseViewCount(ARTWORK_ID, 3L)).willReturn(0);

        assertThat(service.flushOne(ARTWORK_ID)).isFalse();

        verify(artworkViewCountRepository).clear(ARTWORK_ID);
        verify(artworkViewCountRepository, never()).subtractApplied(anyLong(), anyLong());
    }
}
