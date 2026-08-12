package kr.co.dearbloom.domain.member.facade;

import kr.co.dearbloom.domain.artist.dto.artist.request.ArtistCreateRequest;
import kr.co.dearbloom.domain.artist.dto.artist.response.ArtistCreateResponse;
import kr.co.dearbloom.domain.artist.dto.artist.response.ArtistResponse;
import kr.co.dearbloom.domain.artist.entity.artist.Artist;
import kr.co.dearbloom.domain.artist.service.artist.ArtistCommandService;
import kr.co.dearbloom.domain.artist.service.artist.ArtistQueryService;
import kr.co.dearbloom.domain.artist.service.schedule.ScheduleCommandService;
import kr.co.dearbloom.domain.artwork.service.ArtworkWithdrawalService;
import kr.co.dearbloom.domain.auth.dto.TokenRefreshResponse;
import kr.co.dearbloom.domain.auth.service.OAuthAccountService;
import kr.co.dearbloom.domain.auth.service.TokenService;
import kr.co.dearbloom.domain.customer.dto.request.CustomerCreateRequest;
import kr.co.dearbloom.domain.customer.dto.response.CustomerCreateResponse;
import kr.co.dearbloom.domain.customer.dto.response.CustomerResponse;
import kr.co.dearbloom.domain.customer.entity.Customer;
import kr.co.dearbloom.domain.board.service.board.SharedBoardWithdrawalService;
import kr.co.dearbloom.domain.chat.service.ChatWithdrawalService;
import kr.co.dearbloom.domain.customer.service.CustomerCommandService;
import kr.co.dearbloom.domain.customer.service.CustomerQueryService;
import kr.co.dearbloom.domain.customer.service.SavedArtworkCommandService;
import kr.co.dearbloom.domain.inquiry.service.InquiryWithdrawalService;
import kr.co.dearbloom.domain.member.dto.RoleSwitchResponse;
import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.domain.member.entity.MemberRole;
import kr.co.dearbloom.domain.member.service.MemberCommandService;
import kr.co.dearbloom.domain.member.service.MemberQueryService;
import kr.co.dearbloom.domain.notification.service.DeviceTokenCommandService;
import kr.co.dearbloom.domain.member.util.ProfileNameResolver;
import kr.co.dearbloom.domain.university.entity.University;
import kr.co.dearbloom.domain.university.service.UniversityQueryService;
import kr.co.dearbloom.global.auth.jwt.TokenProvider;
import kr.co.dearbloom.global.dto.response.exception.CustomException;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import kr.co.dearbloom.domain.report.service.ReportCommandService;
import kr.co.dearbloom.global.file.FileCleaner;
import kr.co.dearbloom.global.file.FileUrlValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberFacade {
    private final MemberCommandService memberCommandService;
    private final MemberQueryService memberQueryService;
    private final ArtistCommandService artistCommandService;
    private final CustomerCommandService customerCommandService;
    private final UniversityQueryService universityQueryService;
    private final OAuthAccountService oAuthAccountService;
    private final CustomerQueryService customerQueryService;
    private final ArtistQueryService artistQueryService;
    private final SavedArtworkCommandService savedArtworkCommandService;
    private final ScheduleCommandService scheduleCommandService;
    private final ArtworkWithdrawalService artworkWithdrawalService;
    private final ChatWithdrawalService chatWithdrawalService;
    private final ReportCommandService reportCommandService;
    private final FileCleaner fileCleaner;
    private final InquiryWithdrawalService inquiryWithdrawalService;
    private final SharedBoardWithdrawalService sharedBoardWithdrawalService;
    private final DeviceTokenCommandService deviceTokenCommandService;
    private final TokenService tokenService;
    private final TokenProvider tokenProvider;
    private final FileUrlValidator fileUrlValidator;

    /**
     * 고객 ↔ 작가 모드 전환. 대상 role 의 프로필 보유 여부를 서버가 재검증한 뒤
     * recentRole 을 갱신하고 activeRole 을 새 role 로 강제한 Access Token 을 재발급한다.
     * Refresh Token 은 재발급하지 않는다.
     */
    public RoleSwitchResponse switchRole(Member member, MemberRole role) {
        Member updated = memberCommandService.validateRoleAndTouchRecent(member, role);
        String accessToken = tokenService.createAccessToken(updated, role);
        return new RoleSwitchResponse(accessToken, role);
    }

    /**
     * 고객 온보딩. 학교(선택)로 고객 프로필을 만들고,
     * activeRole 이 CUSTOMER 로 갱신된 새 accessToken 을 함께 반환한다.
     * 이름은 요청으로 받지 않고 소셜 계정 이름에서 채운다({@link ProfileNameResolver}).
     */
    @Transactional
    public CustomerCreateResponse createCustomer(Member member, CustomerCreateRequest request) {
        // 학교는 선택. 대학생이 아니면 null 로 보낼 수 있다.
        University university = request.getUniversityId() == null
                ? null
                : universityQueryService.findById(request.getUniversityId());
        Member updated = memberCommandService.markAsCustomer(member);
        Customer customer = customerCommandService.create(
                updated, ProfileNameResolver.resolve(member), university, request.getRegion());
        return new CustomerCreateResponse(
                tokenService.createAccessToken(updated, MemberRole.CUSTOMER),
                CustomerResponse.from(customer)
        );
    }

    /**
     * 작가 온보딩. 활동 지역·대표 이미지(선택)로 작가 프로필을 만들고,
     * activeRole 이 ARTIST 로 갱신된 새 accessToken 을 함께 반환한다.
     * 닉네임은 요청으로 받지 않고 소셜 계정 이름에서 채운다({@link ProfileNameResolver}).
     */
    @Transactional
    public ArtistCreateResponse createArtist(Member member, ArtistCreateRequest request) {
        // 대표 이미지는 선택. 보냈다면 CDN 경로인지 검증한다.
        if (request.getImageUrl() != null) {
            fileUrlValidator.validate(request.getImageUrl());
        }
        // 해지 후 재온보딩이면 익명화된 행을 되살린다. markAsArtist 로 활성/비활성을 판별하므로 create 를 먼저 호출.
        Artist artist = artistCommandService.create(member, ProfileNameResolver.resolve(member), request);
        Member updated = memberCommandService.markAsArtist(member);
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
        // role 은 클라이언트가 보낸 값이라 반드시 검증해야 한다 — 없으면 프로필 없는 role 로도 토큰이 발급된다.
        Member updated = memberCommandService.validateRoleAndTouchRecent(member, role);
        String newAccessToken = tokenService.createAccessToken(updated, role);
        return new TokenRefreshResponse(newAccessToken, refreshToken);
    }


    /**
     * 로그아웃. Redis 의 refreshToken 세션을 삭제해 무효화하고, 이 회원의 푸시 토큰도 모두 지운다.
     * 토큰을 남기면 로그아웃한 기기로 남의 알림이 계속 간다.
     */
    @Transactional
    public void logout(Long memberId) {
        deviceTokenCommandService.deleteAllOf(memberId);
        tokenService.logout(memberId);
    }

//    /**
//     * 고객 역할 해지. 마지막 남은 역할이면 계정 전체 탈퇴로 수렴한다(withdrawn=true).
//     * 작가 역할이 남아 있으면 고객 프로필만 익명화·플래그 해제하고, 남은 작가 역할로 accessToken 을 재발급한다.
//     */
//    @Transactional
//    public RoleRevokeResponse revokeCustomerRole(Member member) {
//        if (!member.isHasCustomer()) {
//            throw new CustomException(ErrorCode.ROLE_NOT_AVAILABLE);
//        }
//        if (!member.isHasArtist()) { // 마지막 역할 → 회원 탈퇴로 수렴
//            withdraw(member);
//            return RoleRevokeResponse.asWithdrawn();
//        }
//        inquiryWithdrawalService.cancelForCustomerRevoke(member); // 고객으로 걸린 진행중/예약 자동 취소
//        customerCommandService.anonymizeByMember(member);
//        Member updated = memberCommandService.revokeRole(member, MemberRole.CUSTOMER);
//        String accessToken = tokenService.createAccessToken(updated, MemberRole.ARTIST);
//        return RoleRevokeResponse.switched(accessToken, MemberRole.ARTIST);
//    }

//    /**
//     * 작가 역할 해지. 마지막 남은 역할이면 계정 전체 탈퇴로 수렴한다(withdrawn=true).
//     * 고객 역할이 남아 있으면 작가 프로필만 익명화·플래그 해제하고, 남은 고객 역할로 accessToken 을 재발급한다.
//     */
//    @Transactional
//    public RoleRevokeResponse revokeArtistRole(Member member) {
//        if (!member.isHasArtist()) {
//            throw new CustomException(ErrorCode.ROLE_NOT_AVAILABLE);
//        }
//        if (!member.isHasCustomer()) { // 마지막 역할 → 회원 탈퇴로 수렴
//            withdraw(member);
//            return RoleRevokeResponse.asWithdrawn();
//        }
//        inquiryWithdrawalService.cancelForArtistRevoke(member); // 작가로 걸린 진행중/예약 자동 취소
//        artistCommandService.anonymizeByMember(member);
//        Member updated = memberCommandService.revokeRole(member, MemberRole.ARTIST);
//        String accessToken = tokenService.createAccessToken(updated, MemberRole.CUSTOMER);
//        return RoleRevokeResponse.switched(accessToken, MemberRole.CUSTOMER);
//    }
    
    @Transactional
    public void withdraw(Member member) {
        Long memberId = member.getMemberId();
        // 푸시 토큰 제거(기기에 남의 알림이 가지 않도록).
        deviceTokenCommandService.deleteAllOf(memberId);
        // 고객·작가 어느 쪽으로든 걸린 진행중/예약완료 문의 자동 취소(익명화 전에 먼저).
        inquiryWithdrawalService.cancelAllForWithdrawal(member);
        // Apple refresh token 폐기(외부호출). 실패해도 탈퇴는 진행한다.
        try {
            oAuthAccountService.revokeAppleTokenIfPresent(member);
        } catch (Exception e) {
            log.warn("[Withdraw] Apple 토큰 revoke 실패(무시하고 탈퇴 진행) — memberId={}, {}", memberId, e.getMessage());
        }
        // 공동보드 정리 — 혼자면 보드 삭제, 남는 멤버가 있으면 방장 위임 후 본인 흔적만 삭제(익명화 전에 먼저).
        sharedBoardWithdrawalService.cleanUpForWithdrawal(member);
        reportCommandService.deleteByReporter(member);       // 신고자 FK 가 Member 라 행이 남으면 탈퇴자와 계속 이어진다
        customerQueryService.findByMember(member).ifPresent(this::deleteCustomerOwnedData);
        artistQueryService.findByMember(member).ifPresent(this::deleteArtistOwnedData);
        oAuthAccountService.deleteByMember(member);          // 소셜 연결 제거
        if (member.isHasCustomer()) {
            customerCommandService.anonymizeByMember(member); // 고객 프로필 익명화
        }
        if (member.isHasArtist()) {
            artistCommandService.anonymizeByMember(member);   // 작가 프로필 익명화
        }
        memberCommandService.withdraw(memberId);             // 멤버 soft delete + PII 제거
        tokenService.logout(memberId);                       // Redis refresh 세션 삭제
    }

    // 고객으로서 남긴 데이터. 저장 작품은 본인만 보는 데이터라 삭제하고, 채팅 사진은 S3 객체만 지운다(대화 행은 유지).
    private void deleteCustomerOwnedData(Customer customer) {
        savedArtworkCommandService.deleteByCustomer(customer);
        chatWithdrawalService.deleteUploadedImages(customer);
    }

    // 작가로서 남긴 데이터. 작품·사진은 통째로 삭제, 대표 이미지도 S3 에서 제거.
    private void deleteArtistOwnedData(Artist artist) {
        artworkWithdrawalService.deleteAllByArtist(artist);
        scheduleCommandService.deleteByArtist(artist);
        chatWithdrawalService.deleteUploadedImages(artist);
        fileCleaner.deleteQuietly(artist.getImageUrl());
    }
}
