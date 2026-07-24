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
