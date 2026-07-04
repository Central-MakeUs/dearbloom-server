package kr.co.dearbloom.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TokenRefreshResponse {
    private String accessToken;
    // 회전(rotation) 미구현 상태라 요청 시 받은 refreshToken 을 그대로 되돌려준다.
    // RefreshTokenSessionService 의 rotation 전용 메서드(find/save(entity))가 주석 처리되어 있어,
    // 필요해지면 그쪽부터 주석 해제한 뒤 이 값을 새로 회전된 토큰으로 교체할 것.
    private String refreshToken;
}
