package kr.co.dearbloom.domain.notification.service;

import kr.co.dearbloom.domain.notification.entity.DevicePlatform;
import kr.co.dearbloom.domain.notification.entity.DeviceToken;
import kr.co.dearbloom.domain.notification.repository.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 디바이스 토큰 조회. */
@Service
@RequiredArgsConstructor
public class DeviceTokenQueryService {
    private final DeviceTokenRepository deviceTokenRepository;

    /**
     * 발송 대상 토큰.
     *
     * <p><b>1차 범위가 iOS 뿐이라 IOS 로 좁힌다.</b> 앱이 Android 에서 토큰을 요청하지 않으므로
     * 실제로는 저장된 Android 토큰이 없지만, 나중에 켤 때 여기 필터만 걷어내면 되도록 명시해 둔다.
     */
    @Transactional(readOnly = true)
    public List<DeviceToken> findSendTargets(Long memberId) {
        return deviceTokenRepository.findAllByMember_MemberIdAndPlatform(memberId, DevicePlatform.IOS);
    }
}
