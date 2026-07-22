package kr.co.dearbloom.global.dev.service;

import jakarta.servlet.http.HttpServletRequest;
import kr.co.dearbloom.domain.artist.dto.artist.response.ArtistResponse;
import kr.co.dearbloom.domain.artist.entity.artist.Artist;
import kr.co.dearbloom.domain.artist.repository.ArtistRepository;
import kr.co.dearbloom.domain.customer.dto.response.CustomerResponse;
import kr.co.dearbloom.domain.customer.entity.Customer;
import kr.co.dearbloom.domain.customer.repository.CustomerRepository;
import kr.co.dearbloom.domain.member.dto.MemberInfoResponse;
import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.domain.member.entity.MemberRole;
import kr.co.dearbloom.domain.member.repository.MemberRepository;
import kr.co.dearbloom.domain.member.service.MemberCommandService;
import kr.co.dearbloom.domain.member.service.MemberQueryService;
import kr.co.dearbloom.domain.auth.entity.OAuthAccount;
import kr.co.dearbloom.domain.auth.service.OAuthAccountService;
import kr.co.dearbloom.domain.auth.service.RefreshTokenSessionService;
import kr.co.dearbloom.global.auth.jwt.TokenProvider;
import kr.co.dearbloom.global.dev.dto.DevLoginRole;
import kr.co.dearbloom.global.dev.dto.DevLoginResponse;
import kr.co.dearbloom.global.dev.dto.DevMemberAccountResponse;
import kr.co.dearbloom.global.dev.dto.DevMemberFullInfoResponse;
import kr.co.dearbloom.global.dto.response.exception.CustomException;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import kr.co.dearbloom.global.properties.JwtProperties;
import kr.co.dearbloom.global.util.HttpRequestUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DevMemberService {
    private final MemberRepository memberRepository;
    private final MemberQueryService memberQueryService;
    private final MemberCommandService memberCommandService;
    private final OAuthAccountService oAuthAccountService;
    private final CustomerRepository customerRepository;
    private final ArtistRepository artistRepository;
    private final TokenProvider tokenProvider;
    private final RefreshTokenSessionService refreshTokenSessionService;
    private final JwtProperties jwtProperties;

    // 시딩된 테스트 계정 전체 목록. hasCustomer/hasArtist 로 어떤 화면 테스트가 가능한지 바로 확인.
    public List<DevMemberAccountResponse> getTestAccounts() {
        return memberRepository.findAll().stream()
                .map(member -> DevMemberAccountResponse.of(
                        member,
                        member.isHasCustomer(),
                        member.isHasArtist()))
                .toList();
    }

    // 테스트 계정으로 즉시 로그인(토큰 발급). role 지정 시 해당 상태로 강제 — 계정이 그 조건을 만족해야 함.
    public DevLoginResponse login(Long memberId, DevLoginRole role, HttpServletRequest request) {
        Member member = memberQueryService.getByMemberIdOrThrow(memberId);
        List<MemberRole> availableRoles = memberQueryService.getAvailableRoles(member);

        if (role == DevLoginRole.ONBOARDING && !availableRoles.isEmpty()) {
            throw new CustomException(ErrorCode.PARAMETER_BAD_REQUEST,
                    "해당 계정은 이미 " + availableRoles + " 프로필이 있어 온보딩 상태로 로그인할 수 없습니다."
                            + " POST /dev/member/signup 으로 새 계정을 만들어 주세요.");
        }
        if (role != null && role != DevLoginRole.ONBOARDING && !availableRoles.contains(role.toMemberRole())) {
            throw new CustomException(ErrorCode.PARAMETER_BAD_REQUEST,
                    "해당 계정은 " + role + " Role 을 갖고 있지 않습니다.");
        }

        return issueTokens(member, role == null ? null : role.toMemberRole(), request);
    }

    /**
     * 프로필(Customer/Artist)이 하나도 없는 새 계정을 만들고 바로 로그인한다.
     * 온보딩은 계정당 한 번뿐이라 재테스트하려면 매번 새 계정이 필요하다.
     */
    @Transactional
    public DevLoginResponse signup(String name, HttpServletRequest request) {
        String unique = name + "-" + System.currentTimeMillis();
        OAuthAccount oauthAccount = oAuthAccountService.createSampleAccount(unique, unique + "@dev.dearbloom.co.kr");
        Member member = memberCommandService.createSampleMember(oauthAccount, name);
        oAuthAccountService.linkMember(oauthAccount, member);

        // 프로필이 없으므로 activeRole/activeProfileId 가 비어 있는 온보딩 상태 토큰이 나온다.
        return issueTokens(member, null, request);
    }

    private DevLoginResponse issueTokens(Member member, MemberRole activeRole, HttpServletRequest request) {
        String accessToken = tokenProvider.generateToken(member, jwtProperties.accessTokenExpiry(), activeRole);
        String refreshToken = tokenProvider.generateToken(member, jwtProperties.refreshTokenExpiry(), activeRole);

        String ip = HttpRequestUtils.extractClientIp(request);
        String deviceInfo = request.getHeader("User-Agent");
        refreshTokenSessionService.save(member, refreshToken, ip, deviceInfo);

        return new DevLoginResponse(accessToken, refreshToken);
    }

    // 인증된 Member 의 Member/Customer/Artist 기본 정보를 한 번에 확인 (개발 편의용)
    public DevMemberFullInfoResponse getMyFullInfo(Member member) {
        Customer customer = customerRepository.findByMember(member).orElse(null);
        Artist artist = artistRepository.findByMember(member).orElse(null);

        return new DevMemberFullInfoResponse(
                MemberInfoResponse.from(member),
                CustomerResponse.from(customer),
                ArtistResponse.from(artist)
        );
    }
}
