package kr.co.dearbloom.domain.artwork.util;

import kr.co.dearbloom.domain.artwork.dto.request.ArtworkPackageRequest;
import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.domain.artwork.entity.ArtworkPackage;
import org.springframework.stereotype.Component;

import java.util.List;

/** 패키지 요청 목록을 ArtworkPackage 엔티티로 변환한다. 작품 등록/수정에서 재사용. */
@Component
public class ArtworkPackageFactory {
    public List<ArtworkPackage> create(Artwork artwork, List<ArtworkPackageRequest> packageList) {
        return packageList.stream()
                .map(request -> ArtworkPackage.builder()
                        .artwork(artwork)
                        .packageName(request.getPackageName())
                        .price(request.getPrice())
                        .durationMinutes(request.getDurationMinutes())
                        .finalPhotoCount(request.getFinalPhotoCount())
                        .extraInfo(request.getExtraInfo())
                        .build())
                .toList();
    }
}
