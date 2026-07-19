package kr.co.dearbloom.domain.artwork.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.dearbloom.domain.artist.entity.Artist;

import java.util.List;

/** 작품 상세에 포함되는 작가 정보. */
public record ArtworkArtistResponse(
        @Schema(description = "작가 ID", example = "1")
        Long artistId,

        @Schema(description = "작가 닉네임", example = "블루밍데이즈 스냅")
        String nickname,

        @Schema(description = "작가 소개. 미등록 시 null.", example = "촬영 전 상담부터 보정까지 정성을 담아 진행합니다.")
        String intro,

        @Schema(description = "작가 활동 지역 목록", example = "[\"SEOUL\", \"GYEONGGI\"]")
        List<String> regionList
) {
    public static ArtworkArtistResponse from(Artist artist) {
        return new ArtworkArtistResponse(
                artist.getArtistId(),
                artist.getNickname(),
                artist.getIntro(),
                artist.getRegions().stream().map(Enum::name).toList()
        );
    }
}
