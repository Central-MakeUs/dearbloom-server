package kr.co.dearbloom.domain.chat.dto;

import kr.co.dearbloom.domain.member.entity.MemberRole;

/**
 * 채팅 접근 주체 — 현재 토큰의 activeRole 과 그 역할의 프로필 PK(customerId/artistId).
 * 채팅은 고객/작가 대칭이라 방 조작 API 는 이 값으로 "내 편"을 판별한다.
 */
public record ChatParticipant(MemberRole role, Long profileId) {
}
