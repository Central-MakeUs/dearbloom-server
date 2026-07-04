package kr.co.dearbloom.domain.member.dto;

import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.domain.member.entity.MemberRole;
import kr.co.dearbloom.domain.member.entity.OAuthProvider;

public record MemberInfoResponse(
        Long memberId,
        String email,
        String name,
        MemberRole recentRole,
        OAuthProvider recentProvider,
        boolean hasCustomer,
        boolean hasArtist
) {
    public static MemberInfoResponse from(Member member) {
        return new MemberInfoResponse(
                member.getMemberId(),
                member.getEmail(),
                member.getName(),
                member.getRecentRole(),
                member.getRecentProvider(),
                member.isHasCustomer(),
                member.isHasArtist()
        );
    }
}
