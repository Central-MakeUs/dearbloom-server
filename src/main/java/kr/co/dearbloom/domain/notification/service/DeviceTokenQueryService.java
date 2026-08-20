package kr.co.dearbloom.domain.notification.service;

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

    /** 발송 대상 토큰. 한 회원이 iOS·Android 기기를 함께 쓸 수 있어 플랫폼을 가리지 않는다. */
    @Transactional(readOnly = true)
    public List<DeviceToken> findSendTargets(Long memberId) {
        return deviceTokenRepository.findAllByMember_MemberId(memberId);
    }
}
