package kr.co.dearbloom.domain.artist.dto.response;

import kr.co.dearbloom.domain.artist.entity.Artist;

import java.util.List;

public record ArtistProfileResponse(
        Long artistId,
        String nickname,
        String intro,
        List<String> regions,
        String profileImageUrl
) {
    public static ArtistProfileResponse from(Artist artist) {
        if (artist == null) {
            return null;
        }
        return new ArtistProfileResponse(
                artist.getArtistId(),
                artist.getNickname(),
                artist.getIntro(),
                artist.getRegions().stream().map(Enum::name).toList(),
                artist.getProfileImageUrl()
        );
    }
}
