package kr.co.dearbloom.domain.artist.entity.work;

import jakarta.persistence.*;
import kr.co.dearbloom.domain.artist.entity.artist.Artist;
import lombok.*;

import java.math.BigDecimal;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
public class Work {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long workId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id", nullable = false)
    private Artist artist;

    private Integer price;

    private Integer originalCount;

    private Integer retouchedCount;

    private Integer shootingTime;
}
