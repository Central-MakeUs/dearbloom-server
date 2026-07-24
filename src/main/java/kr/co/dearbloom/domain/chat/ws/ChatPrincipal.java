package kr.co.dearbloom.domain.chat.ws;

import kr.co.dearbloom.domain.member.entity.MemberRole;

import java.security.Principal;

/** WebSocket 세션 주체 — CONNECT 때 토큰에서 뽑은 (memberId, activeRole, profileId). */
public record ChatPrincipal(Long memberId, MemberRole role, Long profileId) implements Principal {
    @Override
    public String getName() {
        return String.valueOf(memberId);
    }
}
