package kr.co.dearbloom.domain.artist.facade;

import kr.co.dearbloom.domain.artist.dto.response.ArtistProfileResponse;
import kr.co.dearbloom.domain.artist.entity.Artist;
import kr.co.dearbloom.domain.artist.service.ArtistCommandService;
import kr.co.dearbloom.global.file.FileUrlValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArtistFacade {
    private final ArtistCommandService artistCommandService;
    private final FileUrlValidator fileUrlValidator;

    public ArtistProfileResponse updateProfileImage(Artist artist, String profileImageUrl) {
        fileUrlValidator.validate(profileImageUrl);
        return ArtistProfileResponse.from(
                artistCommandService.updateProfileImage(artist, profileImageUrl)
        );
    }
}
