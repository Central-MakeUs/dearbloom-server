package kr.co.dearbloom.domain.artwork.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** 작품 탐색 목록 한 페이지. 무한스크롤은 nextCursor 를 그대로 다음 요청에 실어 이어간다. */
public record ArtworkPageResponse(
        @Schema(description = "작품 카드 목록")
        List<ArtworkSummaryResponse> artworkList,

        @Schema(description = "같은 필터를 만족하는 전체 작품 수(현재 페이지 크기와 무관). 화면 상단 \"전체 N\" 에 사용.",
                example = "31")
        Long totalCount,

        @Schema(description = "다음 페이지 커서. hasNext 가 false 면 null.")
        String nextCursor,

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext
) {
}
