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
    @Schema(description = "소셜 로그인 프로바이더 (현재 GOOGLE 만 지원, APPLE 준비 중)", example = "GOOGLE")
    private OAuthProvider provider;

    @NotBlank
    @Schema(description = "소셜 SDK 토큰 (Google: serverAuthCode, Apple: authorizationCode)")
    private String token; // Google: serverAuthCode, Apple: authorizationCode
}
