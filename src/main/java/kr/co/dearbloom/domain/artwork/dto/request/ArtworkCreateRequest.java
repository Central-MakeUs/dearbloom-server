package kr.co.dearbloom.domain.artwork.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
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

    @NotEmpty(message = "사진을 1장 이상 등록해주세요")
    @Valid
    @Schema(description = "작품 사진 목록. 등록 순서대로 정렬됩니다.")
    private List<ArtworkPhotoRequest> photoList;
}
