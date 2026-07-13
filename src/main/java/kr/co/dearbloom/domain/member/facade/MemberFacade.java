package kr.co.dearbloom.domain.member.facade;

import kr.co.dearbloom.domain.auth.service.TokenService;
import kr.co.dearbloom.domain.member.dto.RoleSwitchResponse;
import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.domain.member.entity.MemberRole;
import kr.co.dearbloom.domain.member.service.MemberCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberFacade {
    private final MemberCommandService memberCommandService;
    private final TokenService tokenService;

    /**
     * 고객 ↔ 작가 모드 전환. 대상 role 의 프로필 보유 여부를 서버가 재검증한 뒤
     * recentRole 을 갱신하고 activeRole 을 새 role 로 강제한 Access Token 을 재발급한다.
     * Refresh Token 은 재발급하지 않는다 (refresh 사용 시점엔 memberId 만 사용하므로 role 변경과 무관).
     */
    public RoleSwitchResponse switchRole(Member member, MemberRole role) {
        Member updated = memberCommandService.switchActiveRole(member, role);
        String accessToken = tokenService.createAccessToken(updated, role);
        return new RoleSwitchResponse(accessToken, role);
    }
}
