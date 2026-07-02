package kr.co.dearbloom.global.dev.dto;

public record DevLoginResponse(
        String accessToken,
        String refreshToken
) {
}
