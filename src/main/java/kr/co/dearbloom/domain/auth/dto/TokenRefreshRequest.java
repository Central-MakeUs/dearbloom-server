package kr.co.dearbloom.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kr.co.dearbloom.domain.member.entity.MemberRole;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenRefreshRequest {
    @NotBlank
    @Schema(description = "재발급에 사용할 refreshToken")
    private String refreshToken;

    @NotNull
    @Schema(description = "재발급받을 accessToken 의 활동 role (CUSTOMER 또는 ARTIST). "
            + "해당 role 의 프로필이 없으면 403 을 반환합니다.", example = "CUSTOMER")
    private MemberRole role;
}
