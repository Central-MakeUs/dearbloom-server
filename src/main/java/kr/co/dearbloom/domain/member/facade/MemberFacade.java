package kr.co.dearbloom.domain.member.facade;

import kr.co.dearbloom.domain.artist.dto.artist.request.ArtistCreateRequest;
import kr.co.dearbloom.domain.artist.dto.artist.response.ArtistCreateResponse;
import kr.co.dearbloom.domain.artist.dto.artist.response.ArtistResponse;
import kr.co.dearbloom.domain.artist.entity.artist.Artist;
import kr.co.dearbloom.domain.artist.service.artist.ArtistCommandService;
import kr.co.dearbloom.domain.auth.dto.TokenRefreshResponse;
import kr.co.dearbloom.domain.auth.service.TokenService;
import kr.co.dearbloom.domain.customer.dto.request.CustomerCreateRequest;
import kr.co.dearbloom.domain.customer.dto.response.CustomerCreateResponse;
import kr.co.dearbloom.domain.customer.dto.response.CustomerResponse;
import kr.co.dearbloom.domain.customer.entity.Customer;
import kr.co.dearbloom.domain.customer.service.CustomerCommandService;
import kr.co.dearbloom.domain.member.dto.RoleSwitchResponse;
import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.domain.member.entity.MemberRole;
import kr.co.dearbloom.domain.member.service.MemberCommandService;
import kr.co.dearbloom.domain.member.service.MemberQueryService;
import kr.co.dearbloom.domain.university.entity.University;
import kr.co.dearbloom.domain.university.service.UniversityQueryService;
import kr.co.dearbloom.global.auth.jwt.TokenProvider;
import kr.co.dearbloom.global.dto.response.exception.CustomException;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import kr.co.dearbloom.global.file.FileUrlValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class MemberFacade {
    private final MemberCommandService memberCommandService;
    private final MemberQueryService memberQueryService;
    private final ArtistCommandService artistCommandService;
    private final CustomerCommandService customerCommandService;
    private final UniversityQueryService universityQueryService;
    private final TokenService tokenService;
    private final TokenProvider tokenProvider;
    private final FileUrlValidator fileUrlValidator;

    /**
     * 고객 ↔ 작가 모드 전환. 대상 role 의 프로필 보유 여부를 서버가 재검증한 뒤
     * recentRole 을 갱신하고 activeRole 을 새 role 로 강제한 Access Token 을 재발급한다.
     * Refresh Token 은 재발급하지 않는다.
     */
    public RoleSwitchResponse switchRole(Member member, MemberRole role) {
        Member updated = memberCommandService.switchActiveRole(member, role);
        String accessToken = tokenService.createAccessToken(updated, role);
        return new RoleSwitchResponse(accessToken, role);
    }

    /**
     * 고객 온보딩. 실명·학교(선택)로 고객 프로필을 만들고,
     * activeRole 이 CUSTOMER 로 갱신된 새 accessToken 을 함께 반환한다.
     */
    @Transactional
    public CustomerCreateResponse createCustomer(Member member, CustomerCreateRequest request) {
        // 학교는 선택. 대학생이 아니면 null 로 보낼 수 있다.
        University university = request.getUniversityId() == null
                ? null
                : universityQueryService.findById(request.getUniversityId());
        Member updated = memberCommandService.markAsCustomer(member);
        Customer customer = customerCommandService.create(updated, request.getName(), university, request.getRegion());
        return new CustomerCreateResponse(
                tokenService.createAccessToken(updated, MemberRole.CUSTOMER),
                CustomerResponse.from(customer)
        );
    }

    /**
     * 작가 온보딩. 닉네임·활동 지역·대표 이미지(선택)로 작가 프로필을 만들고,
     * activeRole 이 ARTIST 로 갱신된 새 accessToken 을 함께 반환한다.
     */
    @Transactional
    public ArtistCreateResponse createArtist(Member member, ArtistCreateRequest request) {
        // 대표 이미지는 선택. 보냈다면 CDN 경로인지 검증한다.
        if (request.getImageUrl() != null) {
            fileUrlValidator.validate(request.getImageUrl());
        }
        Member updated = memberCommandService.markAsArtist(member);
        Artist artist = artistCommandService.create(updated, request);
        return new ArtistCreateResponse(
                tokenService.createAccessToken(updated, MemberRole.ARTIST),
                ArtistResponse.from(artist)
        );
    }

    /**
     * accessToken 재발급. 재발급 시에도 어느 role(고객/작가)로 활동할지 명시적으로 받는다.
     * 대상 role 의 프로필 보유 여부를 재검증한 뒤 activeRole 을 그 role 로 강제한 accessToken 을 발급한다.
     * (recentRole 은 갱신되지만 토큰 activeRole 결정에는 쓰지 않는다 — 최근 접속 role 확인용 데이터일 뿐)
     * 회전(rotation) 미구현이라 refreshToken 자체는 그대로 돌려준다.
     */
    public TokenRefreshResponse refresh(String refreshToken, MemberRole role) {
        if (!tokenProvider.validToken(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        Long memberId = tokenProvider.getMemberId(refreshToken);
        Member member = memberQueryService.getByMemberIdOrThrow(memberId);
        // 프로필 보유 검증(없으면 ROLE_NOT_AVAILABLE) + recentRole 갱신을 switchActiveRole 로 재사용.
        Member updated = memberCommandService.switchActiveRole(member, role);
        String newAccessToken = tokenService.createAccessToken(updated, role);
        return new TokenRefreshResponse(newAccessToken, refreshToken);
    }

    /** 로그아웃. Redis 의 refreshToken 세션을 삭제해 무효화한다. */
    public void logout(Long memberId) {
        tokenService.logout(memberId);
    }
}
