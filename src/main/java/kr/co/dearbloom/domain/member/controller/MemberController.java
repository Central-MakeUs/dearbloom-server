package kr.co.dearbloom.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.dearbloom.domain.auth.dto.TokenRefreshRequest;
import kr.co.dearbloom.domain.auth.dto.TokenRefreshResponse;
import kr.co.dearbloom.domain.member.dto.MemberInfoResponse;
import kr.co.dearbloom.domain.member.dto.RoleSwitchRequest;
import kr.co.dearbloom.domain.member.dto.RoleSwitchResponse;
import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.domain.member.facade.MemberFacade;
import kr.co.dearbloom.global.dto.response.ApiResponse;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import kr.co.dearbloom.global.swagger.ApiErrorCodes;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "Member", description = "회원 관리 API")
public class MemberController {
    private final MemberFacade memberFacade;

    @GetMapping("/me")
    @Operation(summary = "내 계정 정보 조회", description = "최근 접속 Role과 Customer/Artist 각각의 생성 여부를 함께 반환합니다. <br> "
            + "인증이 필요한 API 입니다. Swagger 우측 상단 Authorize 버튼에 accessToken 을 입력한 뒤 호출해주세요.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "인증 실패 (토큰 누락, 만료, 유효하지 않음)")
    })
    public ResponseEntity<ApiResponse<MemberInfoResponse>> getMyInfo(
            @AuthenticationPrincipal Member member
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(MemberInfoResponse.from(member)
        ));
    }

    @PatchMapping("/me/role")
    @Operation(summary = "역할 전환 (고객 ↔ 작가)", description = "요청한 role 에 대응하는 프로필(Customer/Artist)이 이미 생성되어 있어야 합니다. <br> "
            + "성공 시 activeRole 이 갱신된 새 accessToken 을 반환합니다 — 응답받는 즉시 기존 accessToken 을 교체해야 합니다. <br> "
            + "refreshToken 은 재발급하지 않으며 그대로 사용합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "역할 전환 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "인증 실패 (토큰 누락, 만료, 유효하지 않음)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "요청한 역할에 대한 프로필이 없음 (예: Artist 미생성 상태에서 ARTIST 로 전환 시도)")
    })
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.ROLE_NOT_AVAILABLE})
    public ResponseEntity<ApiResponse<RoleSwitchResponse>> switchRole(
            @AuthenticationPrincipal Member member,
            @RequestBody @Valid RoleSwitchRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(memberFacade.switchRole(member, request.getRole()))
        );
    }

    @PostMapping("/refresh")
    @Operation(summary = "accessToken 재발급", description = "refreshToken 검증 후 새 accessToken 발급")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201", description = "accessToken 재발급 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "refreshToken 이 유효하지 않음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "회원을 찾을 수 없음")
    })
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.MEMBER_NOT_FOUND})
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> createNewAccessToken(
            @RequestBody TokenRefreshRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(memberFacade.refresh(request.getRefreshToken()))
        );
    }

    @DeleteMapping("/logout")
    @Operation(summary = "로그아웃", description = "리프레시 토큰 세션을 삭제해 무효화합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "로그아웃 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "인증 필요 (토큰 없음/만료/유효하지 않음)")
    })
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN})
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal Member member) {
        memberFacade.logout(member.getMemberId());
        return ResponseEntity.ok(ApiResponse.success());
    }
}
