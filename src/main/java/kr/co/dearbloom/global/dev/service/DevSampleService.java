package kr.co.dearbloom.global.dev.service;

import jakarta.servlet.http.HttpServletRequest;
import kr.co.dearbloom.domain.auth.service.RefreshTokenSessionService;
import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.domain.member.service.MemberQueryService;
import kr.co.dearbloom.global.auth.jwt.TokenProvider;
import kr.co.dearbloom.global.dev.dto.DevLoginResponse;
import kr.co.dearbloom.global.dto.response.exception.CustomException;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import kr.co.dearbloom.global.properties.JwtProperties;
import kr.co.dearbloom.global.util.HttpRequestUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Slf4j
@Service
@Profile("prod")
public class DevSampleService {
    private final MemberQueryService memberQueryService;
    private final TokenProvider tokenProvider;
    private final JwtProperties jwtProperties;
    private final RefreshTokenSessionService refreshTokenSessionService;
    private final byte[] accessPassword;

    public DevSampleService(MemberQueryService memberQueryService,
                            TokenProvider tokenProvider,
                            JwtProperties jwtProperties,
                            RefreshTokenSessionService refreshTokenSessionService,
                            @Value("${dev.sample.access-password:}") String accessPassword) {
        this.memberQueryService = memberQueryService;
        this.tokenProvider = tokenProvider;
        this.jwtProperties = jwtProperties;
        this.refreshTokenSessionService = refreshTokenSessionService;
        this.accessPassword = accessPassword == null ? new byte[0] : accessPassword.getBytes(StandardCharsets.UTF_8);
    }

    public DevLoginResponse login(String password, Long memberId, HttpServletRequest request) {
        String ip = HttpRequestUtils.extractClientIp(request);

        if (!matchesPassword(password)) {
            // 어느 쪽이 틀렸는지 알려주지 않는다. 응답으로 memberId 존재 여부를 캐낼 수 없어야 한다.
            log.warn("[DevSample] 비밀번호 불일치 — ip={}, memberId={}", ip, memberId);
            throw new CustomException(ErrorCode.REQUEST_UNAUTHORIZED_ACCESS);
        }
        if (memberId == null || memberId >= 0) {
            log.warn("[DevSample] 샘플 대역이 아닌 memberId 요청 — ip={}, memberId={}", ip, memberId);
            throw new CustomException(ErrorCode.PARAMETER_BAD_REQUEST,
                    "샘플 계정(memberId 가 음수)만 로그인할 수 있습니다.");
        }

        Member member = memberQueryService.getByMemberIdOrThrow(memberId);
        log.info("[DevSample] 샘플 계정 로그인 — ip={}, memberId={}", ip, memberId);

        // activeRole 을 지정하지 않으면 계정이 가진 role 중 첫 번째로 발급된다(샘플은 role 이 하나뿐).
        String accessToken = tokenProvider.generateToken(member, jwtProperties.accessTokenExpiry(), null);
        String refreshToken = tokenProvider.generateToken(member, jwtProperties.refreshTokenExpiry(), null);
        refreshTokenSessionService.save(member, refreshToken, ip, request.getHeader("User-Agent"));

        return new DevLoginResponse(accessToken, refreshToken);
    }

    /**
     * 비밀번호 비교. 설정되지 않았으면 <b>무조건 실패</b>다 — 환경변수 누락이 곧 개방이 되면 안 된다.
     * 길이·내용이 언제 갈리든 같은 시간이 걸리도록 {@link MessageDigest#isEqual} 로 비교한다.
     */
    private boolean matchesPassword(String password) {
        if (accessPassword.length == 0) {
            log.warn("[DevSample] 접근 비밀번호가 설정되지 않아 요청을 거부한다 (dev.sample.access-password)");
            return false;
        }
        if (password == null) {
            return false;
        }
        return MessageDigest.isEqual(accessPassword, password.getBytes(StandardCharsets.UTF_8));
    }
}
