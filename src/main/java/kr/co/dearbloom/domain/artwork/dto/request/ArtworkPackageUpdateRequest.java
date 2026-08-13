package kr.co.dearbloom.domain.artwork.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/** 작품 패키지 전체 교체. 남길 패키지도 함께 보내야 한다(부분 수정이 아니라 목록 통째 교체). */
@Getter
@NoArgsConstructor
@Schema(example = """
        {
          "packageList": [
            {
              "packageName": "패키지A",
              "price": 200000,
              "durationMinutes": 90,
              "finalPhotoCount": 10,
              "extraInfo": null
            },
            {
              "packageName": "패키지B",
              "price": 240000,
              "durationMinutes": 120,
              "finalPhotoCount": 7,
              "extraInfo": "빈티지 디카 추가 촬영 가능"
            }
          ]
        }
        """)
public class ArtworkPackageUpdateRequest {
    @NotEmpty(message = "패키지를 1개 이상 등록해주세요")
    @Valid
    @Schema(description = "교체할 패키지 목록(1개 이상). 이 목록이 기존 패키지를 통째로 대체하며, 리스트 화면의 가격도 이 중 최저가로 갱신됩니다.")
    private List<ArtworkPackageRequest> packageList;
}
