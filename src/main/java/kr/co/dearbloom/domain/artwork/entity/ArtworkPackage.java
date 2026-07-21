package kr.co.dearbloom.domain.artwork.entity;

import jakarta.persistence.*;
import kr.co.dearbloom.global.entity.BaseTime;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
public class ArtworkPackage extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long artworkPackageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artwork_id", nullable = false)
    private Artwork artwork;

    @Column(nullable = false)
    private String packageName;

    private Integer price;

    private Integer durationMinutes;

    private Integer finalPhotoCount;

    @Column(columnDefinition = "TEXT")
    private String extraInfo;
}
