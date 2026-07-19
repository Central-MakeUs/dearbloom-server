package kr.co.dearbloom.global.auth.resolver;

import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.domain.member.entity.MemberRole;

/**
 * 조회 요청의 뷰어 정보. 토큰의 activeRole/activeProfileId 기준으로 "지금 어떤 역할로 조회 중"인지 판단.
 * 비로그인이면 모든 값이 null.
 */
public record ViewerContext(Member member, MemberRole activeRole, Long activeProfileId) {
    public static ViewerContext guest() {
        return new ViewerContext(null, null, null);
    }

    public boolean isArtist() {
        return activeRole == MemberRole.ARTIST && activeProfileId != null;
    }

    public boolean isCustomer() {
        return activeRole == MemberRole.CUSTOMER && activeProfileId != null;
    }
}
