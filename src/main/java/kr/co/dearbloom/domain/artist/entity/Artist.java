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

    // ──────────────── 작가 정보 ────────────────
    @Column(nullable = false)
    private String nickname;

    private String imageUrl;

    private String intro; // 소개

    // 활동지역(다중 선택). artist_region 테이블에 작가별 여러 지역 저장.
    @Builder.Default
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "artist_region", joinColumns = @JoinColumn(name = "artist_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "region", nullable = false)
    private Set<Region> regions = new HashSet<>();

    // ──────────────── 촬영 정보 ────────────────
    // 출장비 안내. 작가가 자유 형식으로 등록.
    @Column(columnDefinition = "TEXT")
    private String travelFeeInfo;

    // 패키지 정보. 작가가 자유 형식으로 등록.
    @Column(columnDefinition = "TEXT")
    private String packageInfo;

    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateIntro(String intro) {
        this.intro = intro;
    }

    // 참조를 갈아끼우지 않고 내용만 교체.
    public void updateRegions(Set<Region> regions) {
        this.regions.clear();
        this.regions.addAll(regions);
    }

    // null 인 항목은 변경하지 않는다(PATCH)
    public void updatePricing(String travelFeeInfo, String packageInfo) {
        if (travelFeeInfo != null) {
            this.travelFeeInfo = travelFeeInfo;
        }
        if (packageInfo != null) {
            this.packageInfo = packageInfo;
        }
    }

    public void deleteTravelFeeInfo() {
        this.travelFeeInfo = null;
    }

    public void deletePackageInfo() {
        this.packageInfo = null;
    }
}
