package kr.co.dearbloom.domain.notification.service;

import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.domain.notification.entity.DevicePlatform;
import kr.co.dearbloom.domain.notification.entity.DeviceToken;
import kr.co.dearbloom.domain.notification.repository.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 디바이스 토큰 등록·삭제. 조회는 {@link DeviceTokenQueryService} 담당. */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceTokenCommandService {
    private final DeviceTokenRepository deviceTokenRepository;

    /**
     * 토큰 등록(멱등). 같은 토큰이 이미 있으면 새로 만들지 않고 소유자를 지금 회원으로 옮긴다.
     *
     * <p>기기 하나를 두 사람이 번갈아 로그인하는 경우가 실제로 생긴다. 이때 행을 새로 만들면
     * 토큰 unique 제약에 걸릴 뿐 아니라, 이전 소유자 앞으로 남의 알림이 가게 된다.
     */
    @Transactional
    public void register(Member member, String token, DevicePlatform platform) {
        deviceTokenRepository.findByToken(token)
                .ifPresentOrElse(
                        existing -> existing.transferTo(member, platform),
                        () -> deviceTokenRepository.save(DeviceToken.builder()
                                .member(member)
                                .token(token)
                                .platform(platform)
                                .build()));
    }

    /** 로그아웃 등으로 이 기기에서만 수신을 끊는다. */
    @Transactional
    public void delete(String token) {
        deviceTokenRepository.deleteByToken(token);
    }

    /**
     * 회원의 모든 토큰 삭제. 로그아웃·탈퇴에서 부른다.
     * 남겨두면 그 기기에 남의 알림이 계속 가므로 반드시 지워야 한다.
     */
    @Transactional
    public void deleteAllOf(Long memberId) {
        deviceTokenRepository.deleteAllByMember_MemberId(memberId);
    }
}
