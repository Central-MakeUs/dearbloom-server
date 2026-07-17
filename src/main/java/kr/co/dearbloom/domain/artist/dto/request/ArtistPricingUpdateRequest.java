package kr.co.dearbloom.domain.artist.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ArtistPricingUpdateRequest {
    @Size(max = 5000, message = "출장비 정보는 5000자 이하여야 합니다")
    @Schema(description = "출장비 안내(자유 형식, 줄바꿈 포함). 보내지 않거나 null이면 변경하지 않습니다.",
            example = "서울 전지역 - 무료\n경기(성남/하남/구리) - 50,000원\n그 외 지역 - 별도 문의 부탁드립니다!")
    private String travelFeeInfo;

    @Size(max = 5000, message = "패키지 정보는 5000자 이하여야 합니다")
    @Schema(description = "패키지 정보(자유 형식, 줄바꿈 포함). 보내지 않거나 null이면 변경하지 않습니다.",
            example = "졸업스냅 [1인, 2인 구분]\n[개인스냅 Basic]\n-최종보정본 7장 + 원본(jpeg) 전체 제공\n-촬영시간 : 60분 진행\n-가격 : 20만원")
    private String packageInfo;
}
