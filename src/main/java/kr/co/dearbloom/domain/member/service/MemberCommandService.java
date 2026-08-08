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

    /** 고객 프로필 생성 직후 호출. hasCustomer 를 올리고 최근 사용 모드를 CUSTOMER 로 맞춘다. */
    public Member markAsCustomer(Member member) {
        member.markAsCustomer();
        member.updateRecentRole(MemberRole.CUSTOMER);
        return memberRepository.save(member);
    }

    /** 작가 프로필 생성 직후 호출. hasArtist 를 올리고 최근 사용 모드를 ARTIST 로 맞춘다. */
    public Member markAsArtist(Member member) {
        member.markAsArtist();
        member.updateRecentRole(MemberRole.ARTIST);
        return memberRepository.save(member);
    }

    /**
     * 요청한 role 로 활동할 자격이 있는지 검증하고 recentRole 을 갱신한다.
     * <p>
     * 이 메서드는 role 을 "전환"하지 않는다. activeRole 은 DB 컬럼이 아니라 Access Token 클레임이고,
     * 토큰 발급 시 TokenProvider 는 넘겨받은 role 을 그대로 신뢰한다(보유 여부를 확인하지 않는다).
     * 따라서 role 을 파라미터로 받아 토큰을 발급하는 경로는 <b>반드시 이 메서드를 먼저 거쳐야 한다</b> —
     * 빠뜨리면 프로필이 없는 role 로도 accessToken 이 발급된다.
     * <p>
     * recentRole 갱신은 최근 접속 role 을 기록하기 위한 부수 효과이고, 토큰 발급은 호출부(MemberFacade)의 책임이다.
     *
     * @throws CustomException 대상 role 의 프로필이 없으면 ROLE_NOT_AVAILABLE
     */
    public Member validateRoleAndTouchRecent(Member member, MemberRole role) {
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

    /**
     * 역할 해지. 해당 role 의 보유 플래그를 내리고, recentRole 이 해지한 role 이면 남은 role 로 옮긴다.
     * 프로필(Customer/Artist) 행 자체의 익명화는 호출부(MemberFacade) 책임. 마지막 역할 해지는 호출부에서 탈퇴로 분기.
     */
    public Member revokeRole(Member member, MemberRole role) {
        MemberRole remaining = (role == MemberRole.CUSTOMER) ? MemberRole.ARTIST : MemberRole.CUSTOMER;
        switch (role) {
            case CUSTOMER -> member.unmarkCustomer();
            case ARTIST -> member.unmarkArtist();
        }
        if (member.getRecentRole() == role) {
            member.updateRecentRole(remaining);
        }
        return memberRepository.save(member);
    }

    // 회원 탈퇴(soft delete). 탈퇴 시각 기록 + 멤버 PII 제거.
    public Member withdraw(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        member.withdraw();
        return memberRepository.save(member);
    }
}
