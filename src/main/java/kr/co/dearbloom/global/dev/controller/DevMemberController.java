package kr.co.dearbloom.global.dev.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.domain.member.entity.MemberRole;
import kr.co.dearbloom.global.dev.dto.DevLoginResponse;
import kr.co.dearbloom.global.dev.dto.DevMemberAccountResponse;
import kr.co.dearbloom.global.dev.dto.DevMemberFullInfoResponse;
import kr.co.dearbloom.global.dev.service.DevMemberService;
import kr.co.dearbloom.global.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/dev/member")
@Tag(name = "Dev - Member", description = "개발 전용 테스트 계정 조회 및 로그인 API")
@Profile("!prod")
public class DevMemberController {
    private final DevMemberService devMemberService;

    @GetMapping("/accounts")
    @Operation(summary = "테스트 계정 목록 조회", description = "DB에 시딩된 테스트 계정 목록과 각 계정의 Customer/Artist 생성 여부를 반환합니다.")
    public ResponseEntity<ApiResponse<List<DevMemberAccountResponse>>> getTestAccounts() {
        return ResponseEntity.ok(ApiResponse.success(devMemberService.getTestAccounts()));
    }

    @PostMapping("/login/{memberId}")
    @Operation(summary = "테스트 계정 로그인", description = "테스트 계정으로 즉시 로그인하여 access/refresh 토큰을 발급합니다. <br>"
            + "role 을 지정하면 해당 Role(고객/작가) 로 activeRole 을 강제합니다 (계정이 그 Role 을 갖고 있어야 함).")
    public ResponseEntity<ApiResponse<DevLoginResponse>> login(
            @PathVariable Long memberId,
            @Parameter(description = "지정할 activeRole (미지정 시 계정의 최근 접속 Role 사용)")
            @RequestParam(required = false) MemberRole role,
            HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(devMemberService.login(memberId, role, request)));
    }

    @GetMapping("/me")
    @Operation(summary = "액세스 토큰으로 내 정보 조회", description = "Member/Customer/Artist 기본 정보를 한 번에 확인합니다 (Customer/Artist 는 생성된 경우에만 값이 채워짐). <br>"
            + "인증이 필요한 API 입니다. Swagger 우측 상단 Authorize 버튼에 Bearer Token을 입력한 뒤 호출해주세요.")
    public ResponseEntity<ApiResponse<DevMemberFullInfoResponse>> getMyFullInfo(
            @AuthenticationPrincipal Member member) {
        return ResponseEntity.ok(ApiResponse.success(devMemberService.getMyFullInfo(member)));
    }
}
