package kr.co.dearbloom.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kr.co.dearbloom.domain.auth.entity.OAuthProvider;
import kr.co.dearbloom.domain.member.entity.MemberRole;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NativeLoginRequest {
    @NotNull
    @Schema(description = "소셜 로그인 프로바이더 (GOOGLE, APPLE 지원)", example = "GOOGLE")
    private OAuthProvider socialProvider;

    @NotBlank
    @Schema(description = "소셜 SDK 토큰 (Google: serverAuthCode, Apple: identityToken(JWT))")
    private String socialToken; // Google: serverAuthCode, Apple: identityToken(JWT)

    @NotNull
    @Schema(description = "로그인 화면에서 선택한 role (CUSTOMER 또는 ARTIST). 온보딩 라우팅에 사용됩니다. "
            + "선택한 role 의 프로필이 아직 없으면 응답의 needsOnboarding=true 로 내려갑니다.",
            example = "CUSTOMER")
    private MemberRole role;
}
