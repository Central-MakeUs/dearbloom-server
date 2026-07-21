package kr.co.dearbloom.domain.artwork.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.dearbloom.domain.artist.entity.Artist;
import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.domain.artwork.entity.ArtworkPackage;
import kr.co.dearbloom.domain.artwork.entity.PortfolioFile;
import kr.co.dearbloom.domain.university.entity.University;

import java.util.List;
import java.util.Objects;

/** 공개/고객용 작품 상세. 작가 본인용 지표(저장 수/조회수)는 별도 응답(ArtistArtworkDetailResponse). */
public record ArtworkDetailResponse(
        @Schema(description = "작품 ID", example = "1")
        Long artworkId,

        @Schema(description = "작품명", example = "야외 개인 졸업스냅")
        String title,

        @Schema(description = "최소 촬영 인원(1~6)", example = "2")
        Integer minHeadCount,

        @Schema(description = "최대 촬영 인원(1~6). null 이면 minHeadCount인 이상(제한 없음).", example = "3")
        Integer maxHeadCount,

        @Schema(description = "작품 사진 목록. sortOrder 오름차순으로 정렬됩니다.")
        List<ArtworkPhotoResponse> photoList,

        @Schema(description = "사진들에 연결된 촬영 학교명 전체(중복 제거). 학교가 없는 사진은 제외됩니다.",
                example = "[\"서울대\", \"연세대\", \"고려대\"]")
        List<String> schoolNameList,

        @Schema(description = "작품 패키지 목록")
        List<ArtworkPackageResponse> packageList,

        @Schema(description = "작가 정보(닉네임 / 소개 / 활동 지역)")
        ArtworkArtistResponse artist,

        @Schema(description = "이 작가의 다른 작품 목록(현재 작품 제외, 저장 많은 순). 각 항목은 작품 ID와 대표 이미지 1장.")
        List<ArtworkThumbnailResponse> otherArtworkList,

        @Schema(description = "내가 저장한 작품인지 여부. 고객 조회 시에만 값이 있고, 비로그인은 null.", example = "false")
        Boolean isSaved
) {
    public static ArtworkDetailResponse of(Artwork artwork, Artist artist, List<PortfolioFile> files,
                                           List<ArtworkPackage> packages,
                                           List<ArtworkThumbnailResponse> otherArtworkList, Boolean isSaved) {
        return new ArtworkDetailResponse(
                artwork.getArtworkId(),
                artwork.getArtworkName(),
                artwork.getMinHeadCount(),
                artwork.getMaxHeadCount(),
                files.stream().map(ArtworkPhotoResponse::from).toList(),
                schoolNames(files),
                packages.stream().map(ArtworkPackageResponse::from).toList(),
                ArtworkArtistResponse.from(artist),
                otherArtworkList,
                isSaved
        );
    }

    static List<String> schoolNames(List<PortfolioFile> files) {
        return files.stream()
                .map(PortfolioFile::getUniversity)
                .filter(Objects::nonNull)
                .map(University::getName)
                .distinct()
                .toList();
    }
}
