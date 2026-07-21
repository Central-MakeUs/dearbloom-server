package kr.co.dearbloom.domain.artwork.entity;

import jakarta.persistence.*;
import kr.co.dearbloom.domain.university.entity.University;
import kr.co.dearbloom.global.entity.BaseTime;
import kr.co.dearbloom.global.file.FileType;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
public class PortfolioFile extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long portfolioFileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artwork_id", nullable = false)
    private Artwork artwork;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_id")
    private University university;

    @Column(nullable = false)
    private String fileUrl;

    @Enumerated(EnumType.STRING)
    private FileType fileType;

    private Integer sortOrder;
}
