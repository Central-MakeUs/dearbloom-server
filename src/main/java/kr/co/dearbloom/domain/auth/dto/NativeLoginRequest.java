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

    @Schema(description = "Apple 전용: SDK 로그인 결과의 authorizationCode. "
            + "탈퇴 시 Apple 토큰 폐기(App Store 필수)를 위해 서버가 refresh token 으로 교환·저장합니다. "
            + "Google 은 불필요, Apple 은 넣어주세요.")
    private String authorizationCode;

    @NotNull
    @Schema(description = "로그인 화면에서 선택한 role (CUSTOMER 또는 ARTIST). 온보딩 라우팅에 사용됩니다. "
            + "선택한 role 의 프로필이 아직 없으면 응답의 needsOnboarding=true 로 내려갑니다.",
            example = "CUSTOMER")
    private MemberRole role;
}
