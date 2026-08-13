package kr.co.dearbloom.domain.notification.facade;

import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.domain.notification.dto.request.DeviceTokenRegisterRequest;
import kr.co.dearbloom.domain.notification.service.DeviceTokenCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 디바이스 토큰 등록·해제 진입점. */
@Component
@RequiredArgsConstructor
public class DeviceTokenFacade {
    private final DeviceTokenCommandService deviceTokenCommandService;

    public void register(Member member, DeviceTokenRegisterRequest request) {
        deviceTokenCommandService.register(member, request.getToken(), request.getPlatform());
    }

    public void unregister(String token) {
        deviceTokenCommandService.delete(token);
    }
}
