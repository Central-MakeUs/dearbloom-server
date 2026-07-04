package kr.co.dearbloom.global.dev.dto;

import kr.co.dearbloom.domain.member.entity.Member;

public record DevMemberAccountResponse(
        Long memberId,
        String name,
        String email,
        boolean hasCustomer,
        boolean hasArtist
) {
    public static DevMemberAccountResponse of(Member member, boolean hasCustomer, boolean hasArtist) {
        return new DevMemberAccountResponse(
                member.getMemberId(),
                member.getName(),
                member.getEmail(),
                hasCustomer,
                hasArtist
        );
    }
}
