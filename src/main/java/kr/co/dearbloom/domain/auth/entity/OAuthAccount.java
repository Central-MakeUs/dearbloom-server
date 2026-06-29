package kr.co.dearbloom.domain.auth.entity;

import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.global.entity.BaseTime;
import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
@Table(name = "oauth_account")
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

    @Column(name = "oauthId", nullable = false, updatable = false, unique = true)
    private String oauthId; // 소셜로그인 ID

    @Column(name = "email", nullable = false, updatable = false) //, unique = true) ToDo
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name="name")
    private String name;

    public void linkMember(Member member) {
        this.member = member;
    }
}
