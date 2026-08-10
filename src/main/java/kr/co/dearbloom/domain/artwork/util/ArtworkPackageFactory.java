package kr.co.dearbloom.domain.artwork.util;

import kr.co.dearbloom.domain.artwork.dto.request.ArtworkPackageRequest;
import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.domain.artwork.entity.ArtworkPackage;
import kr.co.dearbloom.global.dto.response.exception.CustomException;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.List;

/** 패키지 요청 목록을 ArtworkPackage 엔티티로 변환한다. 작품 등록/수정에서 재사용. */
@Component
public class ArtworkPackageFactory {
    /**
     * 패키지 요청들의 최저가 = 작품 카드에 노출할 가격.
     * 패키지 엔티티는 작품이 저장된 뒤에야 만들 수 있어서, 가격은 요청에서 먼저 뽑아 작품과 함께 저장한다.
     * packageList 는 1개 이상·price 필수(@Valid)라 빈 값은 정상 흐름에서 나오지 않는다.
     */
    public Integer lowestPrice(List<ArtworkPackageRequest> packageList) {
        return packageList.stream()
                .map(ArtworkPackageRequest::getPrice)
                .min(Integer::compareTo)
                .orElseThrow(() -> new CustomException(ErrorCode.PARAMETER_BAD_REQUEST));
    }

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
