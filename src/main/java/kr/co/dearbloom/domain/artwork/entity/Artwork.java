package kr.co.dearbloom.domain.artwork.entity;

import jakarta.persistence.*;
import kr.co.dearbloom.domain.artist.entity.Artist;
import kr.co.dearbloom.global.entity.BaseTime;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
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
    private Integer minHeadCount;

    private Integer maxHeadCount;

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
