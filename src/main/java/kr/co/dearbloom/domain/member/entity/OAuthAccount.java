package kr.co.dearbloom.domain.member.entity;

import kr.co.dearbloom.global.entity.BaseTime;
import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
@Table(name = "oauth_account",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_oauth_provider_oauth_id",
                columnNames = {"oauth_provider", "oauth_id"}),
        indexes = @Index(name = "idx_oauth_account_email", columnList = "email"))
public class OAuthAccount extends BaseTime {
    //사용자의 인증 정보와 권한 정보를 저장하는 메서드 제공
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id", updatable = false)
    private Long accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "oauth_provider", nullable = false, updatable = false)
    private OAuthProvider oauthProvider;

    /**
     * 연결된 멤버 (FK 소유측). nullable — OAuth 로그인 시 OAuthAccount 가 Member 보다 먼저 생성됨.
     * 한 Member 가 여러 소셜계정을 연동할 수 있도록 N:1.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "oauth_id", nullable = false, updatable = false)
    private String oauthId; // 소셜로그인 ID

    /**
     * 소셜 계정 이메일. 같은 이메일이면 통합계정으로 병합(서비스 로직).
     * [정책] 애플은 비공개 릴레이 이메일(@privaterelay.appleid.com)을 줄 수 있음.
     * 이 경우 실제 이메일과 달라 다른 provider 계정과 이메일 기반 병합이 불가능함 → 별도 계정으로 취급.
     */
    @Column(name = "email", nullable = false, updatable = false)
    private String email;

    @Column(name="name")
    private String name;

    public void linkMember(Member member) {
        this.member = member;
    }
}
