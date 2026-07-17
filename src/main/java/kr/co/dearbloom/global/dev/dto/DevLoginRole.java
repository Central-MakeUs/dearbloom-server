package kr.co.dearbloom.global.dev.dto;

import kr.co.dearbloom.domain.member.entity.MemberRole;

/**
 * 개발용 로그인에서 고를 수 있는 상태. {@link MemberRole} 은 토큰 클레임에 실리는 도메인 enum 이라
 * 온보딩 같은 테스트 편의 개념을 섞지 않고 dev 계층에만 둔다.
 */
public enum DevLoginRole {
    /** 고객 모드로 로그인. 계정에 Customer 프로필이 있어야 한다. */
    CUSTOMER(MemberRole.CUSTOMER),

    /** 작가 모드로 로그인. 계정에 Artist 프로필이 있어야 한다. */
    ARTIST(MemberRole.ARTIST),

    /** 온보딩 상태(activeRole 없는 토큰)로 로그인. 계정에 프로필이 하나도 없어야 한다. */
    ONBOARDING(null);

    private final MemberRole memberRole;

    DevLoginRole(MemberRole memberRole) {
        this.memberRole = memberRole;
    }

    /** 토큰에 강제할 activeRole. ONBOARDING 이면 null(=강제하지 않음). */
    public MemberRole toMemberRole() {
        return memberRole;
    }
}
