package kr.co.dearbloom.domain.artwork.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** 작품 리스트 항목(카드). 작품탐색, 내 저장 목록, 공동보드, 내 후보 수정하기 리스트 조회에서 사용. */
public record ArtworkSummaryResponse(
        @Schema(description = "작품 ID", example = "1")
        Long artworkId,

        @Schema(description = "작품명", example = "야외 개인 졸업스냅")
        String title,

        @Schema(description = "가격(원). 패키지 중 최저가.", example = "200000")
        Integer lowestPrice,

        @Schema(description = "최소 촬영 인원(1~6)", example = "2")
        Integer minHeadCount,

        @Schema(description = "최대 촬영 인원(1~6). null 이면 minHeadCount인 이상(제한 없음).", example = "3")
        Integer maxHeadCount,

        @Schema(description = "작가 닉네임", example = "블루밍데이즈 스냅")
        String artistNickname,

        @Schema(description = "작가 활동 지역 목록", example = "[\"SEOUL\", \"GYEONGGI_NORTH\"]")
        List<String> artistRegionList,

        @Schema(description = "대표 이미지 CDN URL. photoList 의 첫 번째 사진과 같으며, 사진이 없으면 null.",
                example = "https://dev-cdn.dearbloom.co.kr/artwork/uuid.webp")
        String thumbnailUrl,

        @Schema(description = "이 작품의 사진 CDN URL 전체(등록 순서). 사진이 없으면 빈 배열. "
                + "카드 한 장에 여러 장을 늘어놓는 리스트 형태에서 사용합니다.",
                example = "[\"https://dev-cdn.dearbloom.co.kr/artwork/uuid1.webp\","
                + "\"https://dev-cdn.dearbloom.co.kr/artwork/uuid2.webp\"]")
        List<String> photoList,

        @Schema(description = "내가 저장한 작품인지 여부. 고객 조회 시에만 값이 있고, 비로그인은 null.",
                example = "false")
        Boolean isSaved
) {
    /**
     * 저장 여부만 채운 복사본.
     * <p>
     * 작품 탐색 첫 화면 캐시에는 isSaved 를 담지 않는다 — 사람마다 다른 값이라 담으면 남의 저장 상태가 나간다.
     * 그래서 캐시에는 isSaved 가 null 인 카드를 넣어두고, 조회 시점에 그 사람 기준으로 이 메서드가 덧씌운다.
     */
    public ArtworkSummaryResponse withSaved(boolean saved) {
        return new ArtworkSummaryResponse(artworkId, title, lowestPrice, minHeadCount, maxHeadCount,
                artistNickname, artistRegionList, thumbnailUrl, photoList, saved);
    }
}
