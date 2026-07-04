package kr.co.dearbloom.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NativeLoginRequest {
    @NotBlank
    private String token; // Google: serverAuthCode, Apple: authorizationCode
}
