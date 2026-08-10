package kr.co.dearbloom.domain.artwork.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import kr.co.dearbloom.domain.artist.entity.artist.Region;
import kr.co.dearbloom.domain.artwork.dto.type.ArtworkSortOrder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/** 작품 탐색 목록의 필터·정렬·커서 파라미터. 모든 필터는 선택이고, 안 보내면 그 조건은 적용하지 않는다. */
@Getter
@Setter
@NoArgsConstructor
public class ArtworkQueryRequest {
    private static final int DEFAULT_SIZE = 16;

    @Schema(description = "촬영 희망 기간 시작일(yyyy-MM-dd). endDate 와 항상 함께 보냅니다. 하루만 고른 경우 endDate 를 같은 날로 보냅니다.",
            example = "2026-06-11")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @Schema(description = "촬영 희망 기간 종료일(yyyy-MM-dd). startDate 로부터 최대 30일까지 허용합니다.",
            example = "2026-10-18")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    @Schema(description = "촬영 지역. 작가 활동 지역에 이 지역이 포함된 작품만 조회합니다.",
            example = "SEOUL")
    private Region region;

    @Schema(description = "촬영 희망 인원(1~6). 6 은 화면의 \"6인 이상\"입니다. 작품의 촬영 가능 인원 범위에 이 값이 들어가는 작품만 조회합니다.",
            example = "2")
    @Min(1)
    @Max(6)
    private Integer headCount;

    @Schema(description = "정렬 기준. LATEST(기본순=등록 최신순) / PRICE_LOW(낮은 가격순) / PRICE_HIGH(높은 가격순).",
            defaultValue = "LATEST")
    private ArtworkSortOrder sort = ArtworkSortOrder.LATEST;

    @Schema(description = "커서. 첫 페이지는 비워서 보내고, 다음 페이지부터는 직전 응답의 nextCursor 를 그대로 보냅니다.")
    private String cursor;

    @Schema(description = "페이지 크기(4~40). 기본값은 16 입니다.", defaultValue = "16")
    @Min(4)
    @Max(40)
    private Integer size = DEFAULT_SIZE;

    // sort= / size= 처럼 빈 값으로 오면 Spring 이 null 을 넣는다. 그 경우 기본값을 지키도록 세터에서 막는다.
    public void setSort(ArtworkSortOrder sort) {
        if (sort != null) {
            this.sort = sort;
        }
    }

    public void setSize(Integer size) {
        if (size != null) {
            this.size = size;
        }
    }
}
