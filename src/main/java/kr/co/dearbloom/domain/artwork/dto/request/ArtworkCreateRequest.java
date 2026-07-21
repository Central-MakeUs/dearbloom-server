package kr.co.dearbloom.domain.artwork.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@Schema(example = """
        {
          "title": "봄날의 졸업사진",
          "price": 200000,
          "minHeadCount": 2,
          "maxHeadCount": 3,
          "photoList": [
            {
              "fileUrl": "https://cdn.dearbloom.co.kr/artwork/uuid1.webp",
              "fileType": "IMAGE",
              "universityId": 60
            },
            {
              "fileUrl": "https://cdn.dearbloom.co.kr/artwork/uuid2.webp",
              "fileType": "IMAGE",
              "universityId": 75
            },
            {
              "fileUrl": "https://cdn.dearbloom.co.kr/artwork/uuid3.webp",
              "fileType": "IMAGE",
              "universityId": 75
            },
            {
              "fileUrl": "https://cdn.dearbloom.co.kr/artwork/uuid4.webp",
              "fileType": "IMAGE",
              "universityId": null
            }
          ]
        }
        """)
public class ArtworkCreateRequest {
    @NotBlank
    @Schema(description = "작품 제목", example = "봄날의 졸업사진")
    private String title;

    @NotNull
    @PositiveOrZero
    @Schema(description = "기본 가격(원)", example = "200000")
    private Integer price;

    @NotNull
    @Min(1)
    @Max(6)
    @Schema(description = "최소 촬영 인원(1~6)", example = "2")
    private Integer minHeadCount;

    @Min(1)
    @Max(6)
    @Schema(description = "최대 촬영 인원(1~6). 보내지 않으면 \"minHeadCount인 이상(예: 3인 이상)\"을 의미합니다.",
            example = "4")
    private Integer maxHeadCount;

    @NotEmpty(message = "사진을 1장 이상 등록해주세요")
    @Valid
    @Schema(description = "작품 사진 목록. 등록 순서대로 정렬됩니다.")
    private List<ArtworkPhotoRequest> photoList;

    @AssertTrue(message = "maxHeadCount는 minHeadCount 이상이어야 합니다")
    @Schema(hidden = true)
    public boolean isHeadCountRangeValid() {
        return maxHeadCount == null || minHeadCount == null || maxHeadCount >= minHeadCount;
    }
}
