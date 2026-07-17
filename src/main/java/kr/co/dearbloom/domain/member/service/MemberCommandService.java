package kr.co.dearbloom.domain.member.service;

import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.domain.member.entity.MemberRole;
import kr.co.dearbloom.domain.auth.entity.OAuthAccount;
import kr.co.dearbloom.domain.member.repository.MemberRepository;
import kr.co.dearbloom.global.dto.response.exception.CustomException;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Member 생성/수정/삭제 전용. 조회는 {@link MemberQueryService} 담당. */
@Service
@RequiredArgsConstructor
public class MemberCommandService {
    private final MemberRepository memberRepository;

    // Member 만 생성·저장. OAuthAccount 와의 연결(FK)은 OAuthAccountService.linkMember 로 별도 처리.
    public Member createMember(OAuthAccount oauthAccount) {
        return memberRepository.save(Member.builder()
                .email(oauthAccount.getEmail())
                .name(oauthAccount.getName())
                .build());
    }

    public Member createSampleMember(OAuthAccount oauthAccount, String name) {
        return memberRepository.save(Member.builder()
                .email(oauthAccount.getEmail())
                .name(name)
                .build());
    }

    /** 작가 프로필 생성 직후 호출. hasArtist 를 올리고 최근 사용 모드를 ARTIST 로 맞춘다. */
    public Member markAsArtist(Member member) {
        member.markAsArtist();
        member.updateRecentRole(MemberRole.ARTIST);
        return memberRepository.save(member);
    }

    /**
     * 고객 ↔ 작가 모드 전환. 요청한 role 에 대응하는 프로필(Customer/Artist)이 없으면 거부.
     * recentRole 을 갱신할 뿐 Access Token 재발급은 호출부(MemberFacade)의 책임.
     */
    public Member switchActiveRole(Member member, MemberRole role) {
        boolean hasProfile = switch (role) {
            case CUSTOMER -> member.isHasCustomer();
            case ARTIST -> member.isHasArtist();
        };
        if (!hasProfile) {
            throw new CustomException(ErrorCode.ROLE_NOT_AVAILABLE);
        }
        member.updateRecentRole(role);
        return memberRepository.save(member);
    }
}
