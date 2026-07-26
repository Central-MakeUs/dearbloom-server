package kr.co.dearbloom.domain.inquiry.dto.response.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.dearbloom.domain.artist.dto.schedule.response.DayAvailabilityResponse;
import kr.co.dearbloom.domain.artist.entity.artist.Artist;
import kr.co.dearbloom.domain.artist.util.SlotGrid;
import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.domain.artwork.entity.ArtworkPackage;

import java.util.List;

/**
 * 스마트 문의 화면 진입 시 한 번에 내려주는 준비 정보.
 * 확인 화면 헤더(작품/작가/이미지) + 선택 패키지 메타 + 인원 범위 + 작가 3개월 가용 캘린더.
 */
@Schema(description = "스마트 문의 준비 정보 (문의 화면 진입용)")
public record InquiryPreparationResponse(
        @Schema(description = "작품명", example = "야외 1인 졸업스냅")
        String artworkName,
        @Schema(description = "작가 닉네임", example = "블루밍데이즈 스냅")
        String artistNickname,
        @Schema(description = "작품 이미지(1장) URL")
        String artworkImageUrl,

        @Schema(description = "선택한 패키지 ID")
        Long artworkPackageId,
        @Schema(description = "패키지명", example = "패키지 A")
        String packageName,
        @Schema(description = "패키지 가격", example = "200000")
        Integer price,
        @Schema(description = "촬영 소요시간(분)", example = "60")
        Integer durationMinutes,
        @Schema(description = "연속 선택할 30분 셀 수 (durationMinutes / 30 올림)", example = "2")
        Integer requiredSlotCount,
        @Schema(description = "슬롯 단위(분). 30 고정", example = "30")
        int slotStepMinutes,

        @Schema(description = "촬영 가능 최소 인원", example = "1")
        Integer minHeadCount,
        @Schema(description = "촬영 가능 최대 인원(null이면 제한 없음/이상)", example = "1")
        Integer maxHeadCount,

        @Schema(description = "작가 3개월 가용 캘린더 (날짜별 예약 가능 시간)")
        List<DayAvailabilityResponse> availability
) {
    public static InquiryPreparationResponse of(
            Artwork artwork,
            Artist artist,
            ArtworkPackage artworkPackage,
            String artworkImageUrl,
            List<DayAvailabilityResponse> availability
    ) {
        Integer durationMinutes = artworkPackage.getDurationMinutes();
        return new InquiryPreparationResponse(
                artwork.getArtworkName(),
                artist.getNickname(),
                artworkImageUrl,
                artworkPackage.getArtworkPackageId(),
                artworkPackage.getPackageName(),
                artworkPackage.getPrice(),
                durationMinutes,
                durationMinutes == null ? null : SlotGrid.requiredSlots(durationMinutes),
                SlotGrid.STEP_MINUTES,
                artwork.getMinHeadCount(),
                artwork.getMaxHeadCount(),
                availability);
    }
}
