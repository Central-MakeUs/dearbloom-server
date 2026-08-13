package kr.co.dearbloom.domain.artwork.entity;

import jakarta.persistence.*;
import kr.co.dearbloom.domain.artist.entity.artist.Artist;
import kr.co.dearbloom.global.entity.BaseTime;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
@Table(indexes = {
        // 목록 정렬 키 그대로. 뒤에 artwork_id 를 붙여야 동점 행까지 인덱스 순서로 정해져 커서 페이지네이션이 성립한다.
        @Index(name = "idx_artwork_created", columnList = "created_at, artwork_id"),
        @Index(name = "idx_artwork_price", columnList = "lowest_price, artwork_id")
})
public class Artwork extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long artworkId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id", nullable = false)
    private Artist artist;

    @Column(nullable = false)
    private String artworkName;

    // 촬영 가능 인원. 1~6 범위. maxHeadCount 가 null 이면 "N인 이상"(제한 없음).
    @Column(nullable = false)
    private Integer minHeadCount;

    private Integer maxHeadCount;

    // 카드에 노출하는 가격 = 패키지 중 최저가. 목록 정렬/필터를 SQL 에서 처리하려고 비정규화해 둔다.
    // 패키지는 등록 시 1개 이상 필수고 가격도 필수라 항상 값이 있다.
    @Column(nullable = false)
    private Integer lowestPrice;

    @Column(columnDefinition = "TEXT")
    private String description;

    // 고객이 이 작품을 저장한 수
    @Builder.Default
    @Column(nullable = false)
    private Integer savedCount = 0;

    // 작품 조회수
    @Builder.Default
    @Column(nullable = false)
    private Integer viewCount = 0;

    // null 이면 변경하지 않는다(PATCH)
    public void updateTitle(String title) {
        if (title != null) {
            this.artworkName = title;
        }
    }

    // 패키지가 바뀌면 반드시 함께 갱신한다 — 목록의 가격 표시·정렬이 이 값만 보기 때문.
    public void updateLowestPrice(Integer lowestPrice) {
        this.lowestPrice = lowestPrice;
    }

    // null 이면 변경하지 않는다(PATCH). 빈 문자열이면 설명을 비운다.
    public void updateDescription(String description) {
        if (description != null) {
            this.description = description;
        }
    }

    public void increaseSavedCount() {
        this.savedCount++;
    }

    public void increaseViewCount() {
        this.viewCount++;
    }

    public void decreaseSavedCount() {
        if (this.savedCount > 0) {
            this.savedCount--;
        }
    }
}
