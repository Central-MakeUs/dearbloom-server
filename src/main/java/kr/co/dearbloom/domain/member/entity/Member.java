package kr.co.dearbloom.domain.member.entity;

import kr.co.dearbloom.domain.auth.entity.OAuthAccount;
import kr.co.dearbloom.domain.auth.entity.OAuthProvider;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Member implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    // FK 는 OAuthAccount(oauth_account.member_id) 가 소유. 여기는 역방향(읽기 전용).
    // 한 Member 가 여러 소셜계정을 연동할 수 있음 (1:N).
    @Builder.Default
    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private List<OAuthAccount> oauthAccounts = new ArrayList<>();

    private String email;

    private String name;

    private String password;

    // 최근 접속 권한 (고객/작가 중 마지막으로 사용한 모드 — 재로그인 시 화면 복원용)
    @Enumerated(EnumType.STRING)
    private MemberRole recentRole;

    @Builder.Default
    private boolean hasCustomer = false;

    @Builder.Default
    private boolean hasArtist = false;

    // 최근 접속 소셜 (마지막으로 로그인한 provider)
    @Enumerated(EnumType.STRING)
    private OAuthProvider recentProvider;

    @CreatedDate
    private LocalDateTime createdAt;

    /* ──────────────── implements from UserDetails ──────────────── */
    @Override // 권한 반환
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("user"));
    }

    @Override // 사용자 고유값 반환(이메일)
    public String getUsername() {
        return email;
    }

    @Override // 사용자 패스워드 반환
    public String getPassword() {
        return password;
    }

    @Override // 계정 만료 여부 반환
    public boolean isAccountNonExpired() {
        // 만료되었는지 확인하는 로직
        return true; // true -> 만료되지 않음
    }

    @Override // 계정 잠금 여부 반환
    public boolean isAccountNonLocked() {
        // 계정 잠금되었는지 확인하는 로직
        return true; // true -> 잠금되지 않음
    }

    @Override //패스워드의 만료 여부 반환
    public boolean isCredentialsNonExpired() {
        // 패스워드가 만료되었는지 확인하는 로직
        return true; // true -> 만료되지 않음
    }

    @Override // 계정 사용 가능 여부 반환
    public boolean isEnabled() {
        // 계정이 사용 가능한지 확인하는 로직
        return true; // true -> 사용 가능
    }

    public void updateName(String name) {
        this.name = name;
    }

    // Customer/Artist 생성 서비스 메서드에서만 호출할 것
    public void markAsCustomer() {
        this.hasCustomer = true;
    }

    public void markAsArtist() {
        this.hasArtist = true;
    }
}
