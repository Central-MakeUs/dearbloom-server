package kr.co.dearbloom.domain.artist.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.dearbloom.domain.artist.entity.Artist;

import java.util.List;

/** 작가 정보 상세 조회 응답(프로필 / 작가 정보). */
public record ArtistDetailResponse(
        @Schema(description = "작가 ID", example = "1")
        Long artistId,

        @Schema(description = "작가 닉네임", example = "블루밍데이즈 스냅")
        String nickname,

        @Schema(description = "작가 소개. 미등록 시 null.", example = "촬영 전 상담부터 보정까지 정성을 담아 진행합니다.")
        String intro,

        @Schema(description = "활동 지역 목록", example = "[\"SEOUL\", \"GYEONGGI_NORTH\"]")
        List<String> regionList,

        @Schema(description = "기타 안내(촬영 취소·환불 규정 등, 자유 형식 텍스트). 미등록 시 null.",
                example = "촬영 취소 및 변경은 2주 전까지 전액 환불 가능합니다.")
        String etcInfo,

        @Schema(description = "작가 대표 이미지 CDN URL. 미등록 시 null.",
                example = "https://cdn.dearbloom.co.kr/artist/uuid.webp")
        String imageUrl
) {
    public static ArtistDetailResponse from(Artist artist) {
        if (artist == null) {
            return null;
        }
        return new ArtistDetailResponse(
                artist.getArtistId(),
                artist.getNickname(),
                artist.getIntro(),
                artist.getRegions().stream().map(Enum::name).toList(),
                artist.getEtcInfo(),
                artist.getImageUrl()
        );
    }
}
