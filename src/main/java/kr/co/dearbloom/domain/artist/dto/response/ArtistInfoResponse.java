package kr.co.dearbloom.domain.artist.dto.response;

import kr.co.dearbloom.domain.artist.entity.Artist;

import java.util.List;

public record ArtistInfoResponse(
        Long artistId,
        String nickname,
        String intro,
        List<String> regions,
        String profileImageUrl
) {
    public static ArtistInfoResponse from(Artist artist) {
        if (artist == null) {
            return null;
        }
        return new ArtistInfoResponse(
                artist.getArtistId(),
                artist.getNickname(),
                artist.getIntro(),
                artist.getRegions().stream().map(Enum::name).toList(),
                artist.getProfileImageUrl()
        );
    }
}
