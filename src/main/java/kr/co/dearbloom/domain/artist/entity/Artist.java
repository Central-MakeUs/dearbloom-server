package kr.co.dearbloom.domain.artist.entity;

import jakarta.persistence.*;
import kr.co.dearbloom.domain.member.entity.Member;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
public class Artist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long artistId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private String nickname;

    private String intro;

    // 활동지역(다중 선택). artist_region 테이블에 작가별 여러 지역 저장.
    @Builder.Default
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "artist_region", joinColumns = @JoinColumn(name = "artist_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "region", nullable = false)
    private Set<Region> regions = new HashSet<>();

    private String profileImageUrl;
}
