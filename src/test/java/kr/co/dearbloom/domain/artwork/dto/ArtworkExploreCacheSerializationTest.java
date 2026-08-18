package kr.co.dearbloom.domain.artwork.dto;

import kr.co.dearbloom.domain.artwork.dto.response.ArtworkPageResponse;
import kr.co.dearbloom.domain.artwork.dto.response.ArtworkSummaryResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 작품 탐색 첫 화면 캐시는 응답 DTO 를 JSON 으로 Redis 에 넣는다.
 * 왕복에서 필드가 조용히 빠지면 캐시가 채워진 뒤 <b>모든 방문자</b>에게 그 값이 사라진 채로 나가므로,
 * 왕복이 손실 없는지를 고정해 둔다.
 */
class ArtworkExploreCacheSerializationTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("캐시 JSON 왕복 — 카드의 모든 필드가 그대로 돌아온다")
    void roundTripKeepsEveryField() {
        ArtworkSummaryResponse card = new ArtworkSummaryResponse(
                7L, "1인 연대 졸업 스냅", 90_000, 1, 3,
                "디어리스트 스냅", List.of("SEOUL", "ULSAN", "JEONNAM"),
                "https://dev-cdn.dearbloom.co.kr/artwork/a.webp",
                List.of("https://dev-cdn.dearbloom.co.kr/artwork/a.webp",
                        "https://dev-cdn.dearbloom.co.kr/artwork/b.webp"),
                null); // 캐시에는 isSaved 를 담지 않는다
        ArtworkPageResponse page = new ArtworkPageResponse(List.of(card), 60L, "eyJhIjoxfQ", true);

        ArtworkPageResponse restored =
                objectMapper.readValue(objectMapper.writeValueAsString(page), ArtworkPageResponse.class);

        assertThat(restored).isEqualTo(page);
    }

    @Test
    @DisplayName("사진이 없는 작품(thumbnailUrl null, photoList 빈 배열)도 왕복이 보존된다")
    void roundTripKeepsEmptyPhotos() {
        ArtworkSummaryResponse card = new ArtworkSummaryResponse(
                8L, "플레인 스냅", 110_000, 2, null,
                "오렌지 스튜디오", List.of("SEOUL"), null, List.of(), null);
        ArtworkPageResponse page = new ArtworkPageResponse(List.of(card), 1L, null, false);

        ArtworkPageResponse restored =
                objectMapper.readValue(objectMapper.writeValueAsString(page), ArtworkPageResponse.class);

        assertThat(restored).isEqualTo(page);
        assertThat(restored.artworkList().getFirst().isSaved()).isNull();
    }

    @Test
    @DisplayName("withSaved 는 저장 여부만 바꾸고 나머지 필드는 건드리지 않는다")
    void withSavedChangesOnlySavedFlag() {
        ArtworkSummaryResponse cached = new ArtworkSummaryResponse(
                7L, "1인 이대 졸업 스냅", 90_000, 1, 3,
                "디어리스트 스냅", List.of("SEOUL"), "https://cdn/a.webp", List.of("https://cdn/a.webp"), null);

        ArtworkSummaryResponse filled = cached.withSaved(true);

        assertThat(filled.isSaved()).isTrue();
        assertThat(filled).isEqualTo(new ArtworkSummaryResponse(
                cached.artworkId(), cached.title(), cached.lowestPrice(),
                cached.minHeadCount(), cached.maxHeadCount(), cached.artistNickname(),
                cached.artistRegionList(), cached.thumbnailUrl(), cached.photoList(), true));
        assertThat(cached.isSaved()).isNull(); // 원본은 그대로
    }
}
