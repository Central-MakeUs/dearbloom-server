package kr.co.dearbloom.domain.artist.dto.response;

import kr.co.dearbloom.domain.artist.entity.Artist;

import java.util.List;

public record ArtistResponse(
        Long artistId,
        String nickname,
        String intro,
        List<String> regionList,
        String etcInfo,
        String imageUrl
) {
    public static ArtistResponse from(Artist artist) {
        if (artist == null) {
            return null;
        }
        return new ArtistResponse(
                artist.getArtistId(),
                artist.getNickname(),
                artist.getIntro(),
                artist.getRegions().stream().map(Enum::name).toList(),
                artist.getEtcInfo(),
                artist.getImageUrl()
        );
    }
}
