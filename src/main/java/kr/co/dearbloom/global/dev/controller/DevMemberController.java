package kr.co.dearbloom.global.dev.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.global.dev.dto.DevLoginRole;
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
            + "<b>CUSTOMER / ARTIST</b>: 해당 Role 로 activeRole 을 강제합니다 (계정이 그 Role 을 갖고 있어야 함). <br>"
            + "<b>ONBOARDING</b>: activeRole 없는 토큰을 발급해 온보딩(POST /api/artists) 을 테스트합니다 "
            + "(계정에 프로필이 하나도 없어야 함 — /dev/member/accounts 에서 hasCustomer/hasArtist 가 모두 false 인 계정). <br>"
            + "미지정 시 계정의 최근 접속 Role 을 사용합니다.")
    public ResponseEntity<ApiResponse<DevLoginResponse>> login(
            @PathVariable Long memberId,
            @Parameter(description = "로그인할 상태 (미지정 시 계정의 최근 접속 Role 사용)")
            @RequestParam(required = false) DevLoginRole role,
            HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(devMemberService.login(memberId, role, request)));
    }

    @PostMapping("/signup")
    @Operation(summary = "온보딩용 새 계정 생성 + 로그인",
            description = "프로필(Customer/Artist)이 하나도 없는 새 테스트 계정을 만들고 바로 로그인해 토큰을 발급합니다. <br>"
                    + "온보딩은 계정당 한 번만 가능해서(두 번째부터 409), 재테스트하려면 매번 새 계정이 필요합니다. <br>"
                    + "발급된 토큰으로 바로 <b>POST /api/artists</b> 를 호출하면 됩니다.")
    public ResponseEntity<ApiResponse<DevLoginResponse>> signup(
            @Parameter(description = "테스트 계정 이름 (뒤에 타임스탬프가 붙어 유일해집니다)", example = "온보딩테스트")
            @RequestParam(defaultValue = "온보딩테스트") String name,
            HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(devMemberService.signup(name, request)));
    }

    @GetMapping("/me")
    @Operation(summary = "액세스 토큰으로 내 정보 조회", description = "Member/Customer/Artist 기본 정보를 한 번에 확인합니다 (Customer/Artist 는 생성된 경우에만 값이 채워짐). <br>"
            + "인증이 필요한 API 입니다. Swagger 우측 상단 Authorize 버튼에 Bearer Token을 입력한 뒤 호출해주세요.")
    public ResponseEntity<ApiResponse<DevMemberFullInfoResponse>> getMyFullInfo(
            @AuthenticationPrincipal Member member) {
        return ResponseEntity.ok(ApiResponse.success(devMemberService.getMyFullInfo(member)));
    }
}
