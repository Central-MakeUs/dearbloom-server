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

    private Integer price;

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

    // null 인 항목은 변경하지 않는다(PATCH)
    public void updateBasicInfo(String title, Integer price) {
        if (title != null) {
            this.artworkName = title;
        }
        if (price != null) {
            this.price = price;
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
