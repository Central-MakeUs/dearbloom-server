package kr.co.dearbloom.domain.artist.entity.artist;

import jakarta.persistence.*;
import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.global.entity.BaseTime;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
public class Artist extends BaseTime {
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
    // 목록 조회에서 작가별 regions LAZY 로딩이 N+1 이 되므로 @BatchSize 로 IN 절 묶음 조회.
    @Builder.Default
    @BatchSize(size = 100)
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "artist_region", joinColumns = @JoinColumn(name = "artist_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "region", nullable = false)
    private Set<Region> regions = new HashSet<>();

    // ──────────────── 작가 포인트 ────────────────
    private Integer point;

    // 기타 안내(촬영 취소·환불 규정 등). 작가가 자유 형식으로 등록.
    @Column(columnDefinition = "TEXT")
    private String etcInfo;

    // 출장비 안내. 작가가 자유 형식으로 등록.
    @Column(columnDefinition = "TEXT")
    private String travelFee;

    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // null 이면 변경하지 않는다(PATCH). 빈 문자열은 비우기.
    public void updateEtcInfo(String etcInfo) {
        if (etcInfo != null) {
            this.etcInfo = etcInfo;
        }
    }

    // null 이면 변경하지 않는다(PATCH). 빈 문자열은 비우기.
    public void updateTravelFee(String travelFee) {
        if (travelFee != null) {
            this.travelFee = travelFee;
        }
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

    // 회원 탈퇴 시 작가 프로필 PII/자유텍스트 익명화.
    public void anonymize() {
        this.nickname = "탈퇴한 작가";
        this.imageUrl = null;
        this.intro = null;
        this.etcInfo = null;
        this.travelFee = null;
        this.regions.clear();
    }

    // 역할 해지로 익명화됐던 프로필을 재온보딩 때 되살린다(같은 사람 복귀 — 기존 행·작품 유지). 온보딩 3개 항목만 재설정.
    public void reactivate(String nickname, String imageUrl, Set<Region> regions) {
        this.nickname = nickname;
        this.imageUrl = imageUrl;
        this.regions.clear();
        this.regions.addAll(regions);
    }
}
