package kr.co.dearbloom.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kr.co.dearbloom.domain.auth.entity.OAuthProvider;
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
}
