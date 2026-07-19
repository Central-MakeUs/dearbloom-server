package kr.co.dearbloom.domain.artwork.util;

import kr.co.dearbloom.domain.artwork.dto.request.ArtworkPhotoRequest;
import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.domain.artwork.entity.PortfolioFile;
import kr.co.dearbloom.domain.university.entity.University;
import kr.co.dearbloom.domain.university.service.UniversityQueryService;
import kr.co.dearbloom.global.file.FileUrlValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 사진 요청 목록을 PortfolioFile 엔티티로 변환한다.
 * fileUrl(CDN 경로) 검증과 학교(선택) 조회를 함께 수행하고, 등록 순서를 sortOrder 로 매긴다.
 * 작품 등록/사진 교체 등 여러 곳에서 재사용.
 */
@Component
@RequiredArgsConstructor
public class PortfolioFileFactory {
    private final UniversityQueryService universityQueryService;
    private final FileUrlValidator fileUrlValidator;

    public List<PortfolioFile> create(Artwork artwork, List<ArtworkPhotoRequest> photoList) {
        List<PortfolioFile> portfolioFiles = new ArrayList<>();
        int sortOrder = 0;
        for (ArtworkPhotoRequest photo : photoList) {
            fileUrlValidator.validate(photo.getFileUrl());
            University university = photo.getUniversityId() == null
                    ? null
                    : universityQueryService.findById(photo.getUniversityId());
            portfolioFiles.add(PortfolioFile.builder()
                    .artwork(artwork)
                    .university(university)
                    .fileUrl(photo.getFileUrl())
                    .fileType(photo.getFileType())
                    .sortOrder(sortOrder++)
                    .build());
        }
        return portfolioFiles;
    }
}
