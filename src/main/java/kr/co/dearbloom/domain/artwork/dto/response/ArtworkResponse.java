package kr.co.dearbloom.domain.artwork.dto.response;

import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.domain.artwork.entity.PortfolioFile;

import java.util.List;

/** 작품 기본 정보 응답. 등록/수정/사진교체 결과로 사용. */
public record ArtworkResponse(
        Long artworkId,
        String title,
        Integer price,
        List<ArtworkPhotoResponse> photoList
) {
    public static ArtworkResponse of(Artwork artwork, List<PortfolioFile> files) {
        return new ArtworkResponse(
                artwork.getArtworkId(),
                artwork.getArtworkName(),
                artwork.getPrice(),
                files.stream().map(ArtworkPhotoResponse::from).toList()
        );
    }
}
