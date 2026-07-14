package kr.co.dearbloom.global.dev.service;

import jakarta.servlet.http.HttpServletRequest;
import kr.co.dearbloom.domain.artist.dto.response.ArtistProfileResponse;
import kr.co.dearbloom.domain.artist.entity.Artist;
import kr.co.dearbloom.domain.artist.repository.ArtistRepository;
import kr.co.dearbloom.domain.customer.dto.response.CustomerInfoResponse;
import kr.co.dearbloom.domain.customer.entity.Customer;
import kr.co.dearbloom.domain.customer.repository.CustomerRepository;
import kr.co.dearbloom.domain.member.dto.MemberInfoResponse;
import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.domain.member.entity.MemberRole;
import kr.co.dearbloom.domain.member.repository.MemberRepository;
import kr.co.dearbloom.domain.member.service.MemberQueryService;
import kr.co.dearbloom.domain.auth.service.RefreshTokenSessionService;
import kr.co.dearbloom.global.auth.jwt.TokenProvider;
import kr.co.dearbloom.global.dev.dto.DevLoginResponse;
import kr.co.dearbloom.global.dev.dto.DevMemberAccountResponse;
import kr.co.dearbloom.global.dev.dto.DevMemberFullInfoResponse;
import kr.co.dearbloom.global.dto.response.exception.CustomException;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import kr.co.dearbloom.global.properties.JwtProperties;
import kr.co.dearbloom.global.util.HttpRequestUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DevMemberService {
    private final MemberRepository memberRepository;
    private final MemberQueryService memberQueryService;
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

    // 테스트 계정으로 즉시 로그인(토큰 발급). role 지정 시 해당 Role 로 activeRole 강제 — 계정이 그 Role 을 갖고 있어야 함.
    public DevLoginResponse login(Long memberId, MemberRole role, HttpServletRequest request) {
        Member member = memberQueryService.getByMemberIdOrThrow(memberId);

        if (role != null && !memberQueryService.getAvailableRoles(member).contains(role)) {
            throw new CustomException(ErrorCode.PARAMETER_BAD_REQUEST,
                    "해당 계정은 " + role + " Role 을 갖고 있지 않습니다.");
        }

        String accessToken = tokenProvider.generateToken(member, jwtProperties.accessTokenExpiry(), role);
        String refreshToken = tokenProvider.generateToken(member, jwtProperties.refreshTokenExpiry(), role);

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
                CustomerInfoResponse.from(customer),
                ArtistProfileResponse.from(artist)
        );
    }
}
