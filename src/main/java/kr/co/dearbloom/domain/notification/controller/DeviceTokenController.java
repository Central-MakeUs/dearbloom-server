package kr.co.dearbloom.domain.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.domain.notification.dto.request.DeviceTokenRegisterRequest;
import kr.co.dearbloom.domain.notification.facade.DeviceTokenFacade;
import kr.co.dearbloom.global.dto.response.ApiResponse;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import kr.co.dearbloom.global.swagger.ApiErrorCodes;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications/device-tokens")
@RequiredArgsConstructor
@Tag(name = "1-2 [Common] Notification", description = "푸시 알림 (앱 전용)")
public class DeviceTokenController {
    private final DeviceTokenFacade deviceTokenFacade;

    @PostMapping
    @Operation(summary = "디바이스 토큰 등록",
            description = """
                    앱(네이티브 셸)이 Firebase 에서 받은 FCM 토큰을 등록합니다. 로그인 직후와, 토큰이 갱신될 때마다 호출하세요.<br>
                    <b>멱등합니다</b> — 같은 토큰을 다시 보내면 행이 늘지 않고 소유자를 현재 로그인 회원으로 옮깁니다.
                    (기기 하나를 여러 계정이 번갈아 쓸 때 이전 소유자에게 알림이 가는 것을 막습니다.)<br><br>
                    <b>iOS·Android 모두 발송됩니다.</b> 한 회원이 두 기기를 함께 쓰면 각 기기로 모두 나갑니다.
                    """)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "인증 실패 (토큰 누락, 만료, 유효하지 않음)")
    })
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN})
    public ResponseEntity<ApiResponse<Void>> register(
            @AuthenticationPrincipal Member member,
            @RequestBody @Valid DeviceTokenRegisterRequest request
    ) {
        deviceTokenFacade.register(member, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping
    @Operation(summary = "디바이스 토큰 해제",
            description = "이 기기에서만 수신을 끊습니다. 로그아웃 시 <b>로그아웃 API 보다 먼저</b> 호출하세요 — "
                    + "로그아웃 API 도 그 회원의 토큰을 모두 지우지만, 앱이 토큰을 알고 있을 때 명시적으로 지우는 편이 확실합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "해제 성공 (없는 토큰이어도 200)")
    })
    public ResponseEntity<ApiResponse<Void>> unregister(@RequestParam String token) {
        deviceTokenFacade.unregister(token);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
