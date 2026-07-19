package kr.co.dearbloom.domain.artwork.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.dearbloom.domain.artwork.entity.PortfolioFile;
import kr.co.dearbloom.domain.university.entity.University;
import kr.co.dearbloom.global.file.FileType;

public record ArtworkPhotoResponse(
        @Schema(description = "사진(포트폴리오 파일) ID", example = "10")
        Long portfolioFileId,

        @Schema(description = "사진 CDN URL", example = "https://cdn.dearbloom.co.kr/artwork/uuid.webp")
        String fileUrl,

        @Schema(description = "파일 종류", example = "IMAGE")
        FileType fileType,

        @Schema(description = "이 사진에 라벨링된 학교 ID. 없으면 null.", example = "1")
        Long universityId,

        @Schema(description = "이 사진에 라벨링된 학교명. 없으면 null.", example = "서울대")
        String universityName,

        @Schema(description = "정렬 순서(0부터). 오름차순으로 화면에 표시됩니다.", example = "0")
        Integer sortOrder
) {
    public static ArtworkPhotoResponse from(PortfolioFile file) {
        University university = file.getUniversity();
        return new ArtworkPhotoResponse(
                file.getPortfolioFileId(),
                file.getFileUrl(),
                file.getFileType(),
                university != null ? university.getUniversityId() : null,
                university != null ? university.getName() : null,
                file.getSortOrder()
        );
    }
}
