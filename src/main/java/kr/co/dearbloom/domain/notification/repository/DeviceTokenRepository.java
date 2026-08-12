package kr.co.dearbloom.domain.notification.repository;

import kr.co.dearbloom.domain.notification.entity.DevicePlatform;
import kr.co.dearbloom.domain.notification.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
    Optional<DeviceToken> findByToken(String token);

    /** 발송 대상 조회. 1차 범위가 iOS 뿐이라 플랫폼으로 좁혀 부른다. */
    List<DeviceToken> findAllByMember_MemberIdAndPlatform(Long memberId, DevicePlatform platform);

    void deleteByToken(String token);

    /** 로그아웃·탈퇴 시 그 회원의 토큰을 모두 지운다. 안 지우면 남의 알림이 계속 간다. */
    void deleteAllByMember_MemberId(Long memberId);
}
