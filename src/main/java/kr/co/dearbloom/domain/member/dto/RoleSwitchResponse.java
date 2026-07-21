package kr.co.dearbloom.domain.member.dto;

import kr.co.dearbloom.domain.member.entity.MemberRole;

public record RoleSwitchResponse(
        String accessToken,
        MemberRole activeRole
) {
}
