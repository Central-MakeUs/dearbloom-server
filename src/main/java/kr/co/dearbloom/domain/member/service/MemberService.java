package kr.co.dearbloom.domain.member.service;

import kr.co.dearbloom.domain.member.entity.OAuthAccount;
import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.domain.member.entity.MemberRole;
import kr.co.dearbloom.domain.member.repository.MemberRepository;
import kr.co.dearbloom.global.dto.response.exception.CustomException;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    public boolean notExistsByOauthAccount(OAuthAccount oauthAccount) {
        return memberRepository.findByOauthAccount(oauthAccount).isEmpty();
    }

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

    public Member getByOauthAccount(OAuthAccount oauthAccount) {
        return memberRepository.findByOauthAccount(oauthAccount)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    public Member getByMemberIdOrThrow(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    public Member getByNameOrThrow(String name) {
        return memberRepository.findByName(name)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND, name));
    }

    // 실제로 생성되어 있는 Role 목록 (Customer, Artist 존재 여부 기준)
    public List<MemberRole> getAvailableRoles(Member member) {
        List<MemberRole> roles = new ArrayList<>();
        if (member.isHasCustomer()) {
            roles.add(MemberRole.CUSTOMER);
        }
        if (member.isHasArtist()) {
            roles.add(MemberRole.ARTIST);
        }
        return roles;
    }
}
