package kr.co.dearbloom.domain.artwork.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.dearbloom.domain.artist.entity.Artist;
import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.domain.artwork.entity.PortfolioFile;

import java.util.List;

/** 작가 본인용 작품 상세. 공개 상세 기본 정보 + 저장 수/조회수(작가 전용 지표). */
public record ArtistArtworkDetailResponse(
        @Schema(description = "작품 ID", example = "1")
        Long artworkId,

        @Schema(description = "작품명", example = "야외 개인 졸업스냅")
        String title,

        @Schema(description = "기본 가격(원)", example = "200000")
        Integer price,

        @Schema(description = "최소 촬영 인원(1~6)", example = "2")
        Integer minHeadCount,

        @Schema(description = "최대 촬영 인원(1~6). null 이면 minHeadCount인 이상(제한 없음).", example = "3")
        Integer maxHeadCount,

        @Schema(description = "작품 사진 목록. sortOrder 오름차순으로 정렬됩니다.")
        List<ArtworkPhotoResponse> photoList,

        @Schema(description = "사진들에 연결된 촬영 학교명 전체(중복 제거).", example = "[\"서울대\", \"연세대\"]")
        List<String> schoolNameList,

        @Schema(description = "출장비 안내. 미등록 시 null.")
        String travelFeeInfo,

        @Schema(description = "패키지 정보. 미등록 시 null.")
        String packageInfo,

        @Schema(description = "작가 정보(닉네임 / 소개 / 활동 지역)")
        ArtworkArtistResponse artist,

        @Schema(description = "이 작가의 다른 작품 목록(현재 작품 제외, 저장 많은 순).")
        List<ArtworkThumbnailResponse> otherArtworkList,

        @Schema(description = "저장 수", example = "12")
        Integer savedCount,

        @Schema(description = "조회수 (집계는 추후 추가 예정)", example = "0")
        Integer viewCount
) {
    public static ArtistArtworkDetailResponse of(Artwork artwork, Artist artist, List<PortfolioFile> files,
                                                 List<ArtworkThumbnailResponse> otherArtworkList) {
        return new ArtistArtworkDetailResponse(
                artwork.getArtworkId(),
                artwork.getArtworkName(),
                artwork.getPrice(),
                artwork.getMinHeadCount(),
                artwork.getMaxHeadCount(),
                files.stream().map(ArtworkPhotoResponse::from).toList(),
                ArtworkDetailResponse.schoolNames(files),
                artist.getTravelFeeInfo(),
                artist.getPackageInfo(),
                ArtworkArtistResponse.from(artist),
                otherArtworkList,
                artwork.getSavedCount(),
                artwork.getViewCount()
        );
    }
}
