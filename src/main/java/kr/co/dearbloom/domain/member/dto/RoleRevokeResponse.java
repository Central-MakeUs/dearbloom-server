package kr.co.dearbloom.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.dearbloom.domain.member.entity.MemberRole;

/**
 * 역할 해지 응답.
 * - 남은 역할이 있으면 {@code withdrawn=false} + 남은 역할로 재발급한 accessToken.
 * - 마지막 역할을 해지해 계정 전체가 탈퇴되면 {@code withdrawn=true} (토큰 없음 → 프론트는 로그아웃 처리).
 */
public record RoleRevokeResponse(
        @Schema(description = "true 면 마지막 역할이라 계정 전체가 탈퇴됨(로그아웃 처리 필요). false 면 남은 역할로 계속 이용")
        boolean withdrawn,

        @Schema(description = "남은 역할로 재발급된 accessToken. withdrawn=true 면 null — 즉시 기존 토큰과 교체하세요")
        String accessToken,

        @Schema(description = "해지 후 남은 활성 역할(CUSTOMER/ARTIST). withdrawn=true 면 null")
        MemberRole activeRole
) {
    public static RoleRevokeResponse asWithdrawn() {
        return new RoleRevokeResponse(true, null, null);
    }

    public static RoleRevokeResponse switched(String accessToken, MemberRole activeRole) {
        return new RoleRevokeResponse(false, accessToken, activeRole);
    }
}
