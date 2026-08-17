package kr.co.dearbloom.domain.artist.dto.artist.response;

import kr.co.dearbloom.domain.artist.entity.artist.Artist;
import kr.co.dearbloom.domain.artist.entity.artist.Region;

import java.util.List;

public record ArtistResponse(
        Long artistId,
        String nickname,
        String intro,
        List<String> regionList,
        String etcInfo,
        String travelFee,
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
                Region.toSortedNames(artist.getRegions()),
                artist.getEtcInfo(),
                artist.getTravelFee(),
                artist.getImageUrl()
        );
    }
}
