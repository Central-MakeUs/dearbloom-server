package kr.co.dearbloom.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.domain.member.entity.MemberRole;

/**
 * 소셜 로그인(네이티브) 성공 응답. 토큰 자체는 HttpOnly 쿠키로 내려가고,
 * 여기서는 <b>온보딩 라우팅에 필요한 최소 정보</b>만 담는다.
 */
@Schema(description = "소셜 로그인 결과 (온보딩 라우팅용). 토큰은 HttpOnly 쿠키로 내려갑니다.")
public record SocialLoginResponse(
        @Schema(description = "로그인 화면에서 선택한 role", example = "CUSTOMER")
        MemberRole selectedRole,
        @Schema(description = "이 회원이 고객 프로필을 이미 가지고 있는지", example = "false")
        boolean hasCustomer,
        @Schema(description = "이 회원이 작가 프로필을 이미 가지고 있는지", example = "false")
        boolean hasArtist,
        @Schema(description = "선택한 role 의 프로필이 아직 없어 온보딩이 필요한지. "
                + "true 면 프론트는 selectedRole 에 맞는 온보딩 화면으로 보낸다.", example = "true")
        boolean needsOnboarding
) {
    public static SocialLoginResponse of(Member member, MemberRole selectedRole) {
        boolean hasSelectedProfile =
                selectedRole == MemberRole.CUSTOMER ? member.isHasCustomer() : member.isHasArtist();
        return new SocialLoginResponse(
                selectedRole,
                member.isHasCustomer(),
                member.isHasArtist(),
                !hasSelectedProfile);
    }
}
