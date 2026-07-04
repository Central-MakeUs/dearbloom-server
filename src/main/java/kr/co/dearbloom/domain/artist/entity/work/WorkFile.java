package kr.co.dearbloom.domain.artist.entity.work;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
public class WorkFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long workFileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_id", nullable = false)
    private Work work;

    @Column(nullable = false)
    private String fileUrl;

    private String fileType;

    private Integer sortOrder;

    @Builder.Default
    private Boolean isRepresentative = false;
}
