package kr.co.dearbloom.domain.member.service;

import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.domain.auth.entity.OAuthAccount;
import kr.co.dearbloom.domain.member.repository.MemberRepository;
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
}
