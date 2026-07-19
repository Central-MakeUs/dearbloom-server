package kr.co.dearbloom.domain.artwork.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.dearbloom.domain.artist.entity.Artist;
import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.domain.artwork.entity.PortfolioFile;
import kr.co.dearbloom.domain.university.entity.University;

import java.util.List;
import java.util.Objects;

public record ArtworkDetailResponse(
        @Schema(description = "작품 ID", example = "1")
        Long artworkId,

        @Schema(description = "작품명", example = "야외 개인 졸업스냅")
        String title,

        @Schema(description = "기본 가격(원)", example = "200000")
        Integer price,

        @Schema(description = "작품 사진 목록. sortOrder 오름차순으로 정렬됩니다.")
        List<ArtworkPhotoResponse> photoList,

        @Schema(description = "사진들에 연결된 촬영 학교명 전체(중복 제거). 학교가 없는 사진은 제외됩니다.",
                example = "[\"서울대\", \"연세대\", \"고려대\"]")
        List<String> schoolNameList,

        @Schema(description = "출장비 안내. 작가가 자유 형식으로 등록한 텍스트(줄바꿈 포함). 미등록 시 null.",
                example = "서울 전지역 - 무료\n경기(성남/하남/구리) - 50,000원")
        String travelFeeInfo,

        @Schema(description = "패키지 정보. 작가가 자유 형식으로 등록한 텍스트(줄바꿈 포함). 미등록 시 null.",
                example = "[개인스냅 Basic]\n-최종보정본 7장 + 원본 제공\n-가격 : 20만원")
        String packageInfo,

        @Schema(description = "작가 정보(닉네임 / 소개 / 활동 지역)")
        ArtworkArtistResponse artist,

        @Schema(description = "이 작가의 다른 작품 목록(현재 작품 제외, 저장 많은 순). 각 항목은 작품 ID와 대표 이미지 1장.")
        List<ArtworkThumbnailResponse> otherArtworkList,

        @Schema(description = "작가 본인이 조회하면 true, 그 외(비로그인 등)는 false", example = "false")
        Boolean isMine,

        @Schema(description = "저장 수. 작가 본인이 조회할 때만 값이 있고, 그 외에는 null.", example = "12")
        Integer savedCount,

        @Schema(description = "작품 조회수. 작가 본인이 조회할 때만 값이 있고, 그 외에는 null. (집계는 추후 추가 예정)",
                example = "0")
        Integer viewCount
) {
    public static ArtworkDetailResponse of(Artwork artwork, Artist artist, List<PortfolioFile> files,
                                           List<ArtworkThumbnailResponse> otherArtworkList, boolean isMine) {
        return new ArtworkDetailResponse(
                artwork.getArtworkId(),
                artwork.getArtworkName(),
                artwork.getPrice(),
                files.stream().map(ArtworkPhotoResponse::from).toList(),
                files.stream()
                        .map(PortfolioFile::getUniversity)
                        .filter(Objects::nonNull)
                        .map(University::getName)
                        .distinct()
                        .toList(),
                artist.getTravelFeeInfo(),
                artist.getPackageInfo(),
                ArtworkArtistResponse.from(artist),
                otherArtworkList,
                isMine,
                isMine ? artwork.getSavedCount() : null,
                isMine ? artwork.getViewCount() : null
        );
    }
}
