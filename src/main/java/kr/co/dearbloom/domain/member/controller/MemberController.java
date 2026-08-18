package kr.co.dearbloom.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import kr.co.dearbloom.domain.artist.dto.artist.request.ArtistCreateRequest;
import kr.co.dearbloom.domain.artist.dto.artist.response.ArtistCreateResponse;
import kr.co.dearbloom.domain.artist.dto.artist.response.NicknameAvailabilityResponse;
import kr.co.dearbloom.domain.artist.facade.ArtistFacade;
import kr.co.dearbloom.domain.auth.dto.TokenRefreshRequest;
import kr.co.dearbloom.domain.auth.dto.TokenRefreshResponse;
import kr.co.dearbloom.domain.customer.dto.request.CustomerCreateRequest;
import kr.co.dearbloom.domain.customer.dto.response.CustomerCreateResponse;
import kr.co.dearbloom.domain.member.dto.MemberInfoResponse;
import kr.co.dearbloom.domain.member.dto.RoleSwitchRequest;
import kr.co.dearbloom.domain.member.dto.RoleSwitchResponse;
import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.domain.member.facade.MemberFacade;
import kr.co.dearbloom.global.dto.response.ApiResponse;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import kr.co.dearbloom.global.swagger.ApiErrorCodes;
import kr.co.dearbloom.global.validation.validatator.ValidNickname;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "Member", description = "회원 관리 API")
public class MemberController {
    private final MemberFacade memberFacade;
    private final ArtistFacade artistFacade;

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
    @Operation(summary = "역할 전환 (고객 ↔ 작가)",
            description = """
                    로그인한 상태에서 고객 ↔ 작가 모드를 전환합니다.
                    요청한 role 에 대응하는 프로필(Customer/Artist)이 <b>이미 생성되어 있어야</b> 하며, 없으면 403 입니다
                    (먼저 온보딩 API 로 해당 프로필을 만들어야 합니다).<br>
                    성공 시 activeRole 이 갱신된 <b>새 accessToken</b> 을 반환합니다 —
                    <b>응답받는 즉시 저장해 둔 accessToken 을 이 값으로 교체하세요.</b>
                    교체하지 않으면 이전 역할의 토큰이 그대로 나가 역할 전용 API 가 403 이 됩니다.<br>
                    refreshToken 은 재발급하지 않으니 기존 값을 그대로 쓰면 됩니다.
                    """)
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

    @PostMapping("/customer")
    @Operation(summary = "고객 계정 생성 (온보딩)",
            description = """
                    이름 / 학교 / 지역을 받아 고객 프로필을 생성합니다. 학교는 한 곳만 선택(선택 항목), 지역도 한 곳 선택(선택 항목)입니다.<br>
                    <b>이름</b>은 2-5자의 한글 또는 영문입니다 — 공백과 숫자는 받지 않습니다.
                    실명이라 중복을 허용하며, 이후 프로필 수정 API 로 변경할 수 있습니다.<br>
                    회원가입 직후의 accessToken 으로는 아직 고객 API 를 호출할 수 없으므로, 이 API 는
                    <b>고객 API 호출이 가능한 새 accessToken</b> 을 응답 바디로 함께 반환합니다.<br>
                    <b>응답받는 즉시 저장해 둔 accessToken 을 이 값으로 교체하세요.</b> 그래야 이후 고객 API 가 정상 동작합니다.<br>
                    refreshToken 은 재발급되지 않으니 기존 값을 그대로 쓰면 됩니다.<br>
                    이미 고객 프로필이 있으면 409 를 반환합니다.
                    """)
    @ApiErrorCodes({ErrorCode.EXPIRED_TOKEN, ErrorCode.UNIVERSITY_NOT_FOUND, ErrorCode.CUSTOMER_ALREADY_EXISTS})
    public ResponseEntity<ApiResponse<CustomerCreateResponse>> createCustomer(
            @AuthenticationPrincipal Member member,
            @RequestBody @Valid CustomerCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                memberFacade.createCustomer(member, request)
        ));
    }

    @GetMapping("/artist/nickname/availability")
    @Operation(summary = "작가 닉네임 중복 검사 (온보딩·닉네임 수정)",
            description = """
                    이 닉네임을 쓸 수 있는지 확인합니다. 작가 온보딩과 닉네임 수정 화면에서 입력 중에 호출하세요.<br>
                    <br>
                    <b>available=true</b> 면 등록/수정에 쓸 수 있고, <b>false</b> 면 이미 다른 작가가 쓰고 있습니다.<br>
                    <b>이미 본인이 쓰고 있는 닉네임은 true</b> 입니다 — 수정 화면에서 닉네임을 바꾸지 않고 저장하는 경우가
                    "중복" 으로 보이면 안 되기 때문입니다. 실제 등록/수정 API 의 판정과 같은 규칙입니다.<br>
                    <br>
                    형식(2-12자의 한글·영문·숫자·<code>_</code> 와 단어 사이 공백)에 맞지 않으면 <b>400</b> 을 돌려줍니다.<br>
                    <b>이 API 결과만 믿고 제출하면 안 됩니다.</b> 검사와 제출 사이에 다른 작가가 같은 닉네임을 선점할 수 있어
                    작가 계정 생성/닉네임 수정이 여전히 409 를 낼 수 있습니다.
                    """)
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.PARAMETER_BAD_REQUEST})
    public ResponseEntity<ApiResponse<NicknameAvailabilityResponse>> checkArtistNickname(
            @AuthenticationPrincipal Member member,
            @RequestParam @NotBlank @ValidNickname String nickname
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                artistFacade.checkNicknameAvailability(member, nickname)
        ));
    }

    @PostMapping("/artist")
    @Operation(summary = "작가 계정 생성 (온보딩)",
            description = """
                    닉네임 / 활동 지역 / 대표 이미지를 받아 작가 프로필을 생성합니다.<br>
                    닉네임과 활동 지역은 필수, <b>대표 이미지는 선택</b>입니다 — 보내지 않으면 이미지 없이 생성되며
                    이후 대표 이미지 수정 API 로 등록할 수 있습니다.<br>
                    <b>닉네임</b>은 2-12자의 한글·영문·숫자·<code>_</code> 에 단어 사이 공백까지 허용합니다
                    (앞뒤 공백과 연속 공백은 불가). <b>이미 쓰이는 닉네임이면 409</b> 를 반환하며,
                    이후 닉네임 수정 API 로 변경할 수 있습니다.<br>
                    회원가입 직후의 accessToken 으로는 아직 작가 API 를 호출할 수 없으므로, 이 API 는
                    <b>작가 API 호출이 가능한 새 accessToken</b> 을 응답 바디로 함께 반환합니다.<br>
                    <b>응답받는 즉시 저장해 둔 accessToken 을 이 값으로 교체하세요.</b> 그래야 이후 작가 API 가 정상 동작합니다.<br>
                    refreshToken 은 재발급되지 않으니 기존 값을 그대로 쓰면 됩니다.<br>
                    이미 작가 프로필이 있으면 409 를 반환합니다.<br><br>
                    <b>regions 가능한 값</b><br>
                    SEOUL, GYEONGGI_NORTH, GYEONGGI_SOUTH, INCHEON, BUSAN, DAEGU, GWANGJU, DAEJEON_SEJONG, ULSAN,
                    GANGWON, CHUNGBUK, CHUNGNAM, JEONBUK, JEONNAM, GYEONGBUK, GYEONGNAM, JEJU
                    """)
    @ApiErrorCodes({ErrorCode.EXPIRED_TOKEN, ErrorCode.INVALID_FILE_URL,
            ErrorCode.NICKNAME_ALREADY_EXISTS, ErrorCode.ARTIST_ALREADY_EXISTS})
    public ResponseEntity<ApiResponse<ArtistCreateResponse>> createArtist(
            @AuthenticationPrincipal Member member,
            @RequestBody @Valid ArtistCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                memberFacade.createArtist(member, request)
        ));
    }

    @PostMapping("/refresh")
    @Operation(summary = "액세스 토큰 재발급",
            description = "accessToken 이 만료됐을 때 refreshToken 으로 새 accessToken 을 받습니다.<br>"
                    + "요청 바디에 <b>refreshToken</b> 과 지금 사용할 <b>role(CUSTOMER/ARTIST)</b> 을 함께 보냅니다 — "
                    + "role 은 보통 마지막으로 쓰던 모드(고객/작가)를 그대로 넣으면 됩니다.<br>"
                    + "응답으로 받은 새 accessToken 으로 <b>기존 값을 교체</b>하면 되고, refreshToken 은 그대로 사용합니다.<br>"
                    + "보유하지 않은 role 을 보내면 403 을 반환합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201", description = "accessToken 재발급 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "refreshToken 이 유효하지 않음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "요청한 role 의 프로필이 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "회원을 찾을 수 없음")
    })
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.ROLE_NOT_AVAILABLE, ErrorCode.MEMBER_NOT_FOUND})
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> createNewAccessToken(
            @RequestBody @Valid TokenRefreshRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(memberFacade.refresh(request.getRefreshToken(), request.getRole()))
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

    @DeleteMapping
    @Operation(summary = "회원 탈퇴",
            description = """
                    현재 로그인한 회원을 탈퇴합니다. <b>계정 전체(고객·작가 프로필 모두)</b>가 탈퇴됩니다.<br>
                    소셜 로그인 연결이 끊기고, 모든 기기의 로그인이 즉시 무효화됩니다.
                    보유한 프로필의 개인정보는 익명화되며, 문의·리뷰 등 상대방 기록은 스냅샷으로 보존됩니다.<br>
                    <b>탈퇴 후 같은 소셜 계정으로 다시 로그인하면 새 계정으로 시작</b>합니다(복구 불가).<br>
                    응답 200 을 받으면 저장한 accessToken/refreshToken 을 삭제하고 로그인 화면으로 이동하세요.
                    """)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "탈퇴 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "인증 필요 (토큰 없음/만료/유효하지 않음)")
    })
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.MEMBER_NOT_FOUND})
    public ResponseEntity<ApiResponse<Void>> withdraw(@AuthenticationPrincipal Member member) {
        memberFacade.withdraw(member);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
