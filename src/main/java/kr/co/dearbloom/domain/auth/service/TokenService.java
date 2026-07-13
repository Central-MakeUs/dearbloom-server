package kr.co.dearbloom.domain.auth.service;

import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.domain.member.entity.MemberRole;
import kr.co.dearbloom.global.auth.jwt.TokenProvider;
import kr.co.dearbloom.global.properties.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TokenService {
    private final TokenProvider tokenProvider;
    private final JwtProperties jwtProperties;
    private final RefreshTokenSessionService refreshTokenSessionService;

    public String createAccessToken(Member member) {
        return tokenProvider.generateToken(member, jwtProperties.accessTokenExpiry());
    }

    /** activeRole 을 강제 지정해 Access Token 발급. 역할 전환(switch-role) 시 사용. */
    public String createAccessToken(Member member, MemberRole overrideActiveRole) {
        return tokenProvider.generateToken(member, jwtProperties.accessTokenExpiry(), overrideActiveRole);
    }

    public String issueRefreshToken(Member member, String ip, String deviceInfo) {
        String refreshToken = tokenProvider.generateToken(member, jwtProperties.refreshTokenExpiry());
        refreshTokenSessionService.save(member, refreshToken, ip, deviceInfo);
        return refreshToken;
    }

    /** 로그아웃. Redis 에 저장된 리프레시 토큰 세션을 삭제해 무효화한다. */
    public void logout(Long memberId) {
        refreshTokenSessionService.delete(memberId);
    }

    // 리프레시 토큰 회전(rotation)은 아직 미구현.
    // RefreshTokenSessionService 의 rotation 전용 메서드(find/save(entity))가 주석 처리되어 있어
    // 여기서도 rotate() 는 추가하지 않는다. 필요해지면 그쪽 주석부터 해제할 것.
}
