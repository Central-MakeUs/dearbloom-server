package kr.co.dearbloom.domain.artist.entity;

import jakarta.persistence.*;
import kr.co.dearbloom.domain.university.entity.University;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
public class PortfolioFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long portfolioFileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id", nullable = false)
    private Artist artist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_id")
    private University university;

    @Column(nullable = false)
    private String fileUrl;

    private String fileType;

    private Integer sortOrder;

    @Builder.Default
    private Boolean isRepresentative = false;
}
