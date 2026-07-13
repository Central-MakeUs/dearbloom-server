package kr.co.dearbloom.domain.product.entity;

import jakarta.persistence.*;
import kr.co.dearbloom.domain.artist.entity.Artist;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id", nullable = false)
    private Artist artist;

    @Column(nullable = false)
    private String productName;

    private Integer price;

    @Column(columnDefinition = "TEXT")
    private String description;
}
