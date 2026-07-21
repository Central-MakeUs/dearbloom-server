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
          "minHeadCount": 2,
          "maxHeadCount": 3,
          "packageList": [
            {
              "packageName": "1인 60분 촬영",
              "price": 200000,
              "durationMinutes": 60,
              "finalPhotoCount": 7,
              "extraInfo": null
            },
            {
              "packageName": "1인 90분 촬영",
              "price": 220000,
              "durationMinutes": 90,
              "finalPhotoCount": 9,
              "extraInfo": "빈티지 디카 추가 촬영 가능"
            }
          ],
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
    @Min(1)
    @Max(6)
    @Schema(description = "최소 촬영 인원(1~6)", example = "2")
    private Integer minHeadCount;

    @Min(1)
    @Max(6)
    @Schema(description = "최대 촬영 인원(1~6). 보내지 않으면 \"minHeadCount인 이상(예: 3인 이상)\"을 의미합니다.",
            example = "4")
    private Integer maxHeadCount;

    @NotEmpty(message = "패키지를 1개 이상 등록해주세요")
    @Valid
    @Schema(description = "작품 패키지 목록(1개 이상). 리스트 화면엔 이 중 최저가가 노출됩니다.")
    private List<ArtworkPackageRequest> packageList;

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
