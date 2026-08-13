package kr.co.dearbloom.domain.notification.service;

import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.domain.notification.entity.DevicePlatform;
import kr.co.dearbloom.domain.notification.entity.DeviceToken;
import kr.co.dearbloom.domain.notification.repository.DeviceTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/** 토큰 등록의 멱등성·소유자 이전 검증. */
@ExtendWith(MockitoExtension.class)
class DeviceTokenCommandServiceTest {
    @Mock DeviceTokenRepository deviceTokenRepository;
    @InjectMocks DeviceTokenCommandService deviceTokenCommandService;

    private Member member(String email) {
        return Member.builder().email(email).name("user").build();
    }

    @Test
    void 처음_보는_토큰은_새로_저장한다() {
        Member member = member("new@example.com");
        given(deviceTokenRepository.findByToken("t-1")).willReturn(Optional.empty());

        deviceTokenCommandService.register(member, "t-1", DevicePlatform.IOS);

        verify(deviceTokenRepository).save(any(DeviceToken.class));
    }

    @Test
    void 같은_기기가_다른_계정으로_재등록되면_행을_늘리지_않고_소유자를_옮긴다() {
        Member previousOwner = member("before@example.com");
        Member newOwner = member("after@example.com");
        DeviceToken existing = DeviceToken.builder()
                .member(previousOwner)
                .token("t-1")
                .platform(DevicePlatform.IOS)
                .build();
        given(deviceTokenRepository.findByToken("t-1")).willReturn(Optional.of(existing));

        deviceTokenCommandService.register(newOwner, "t-1", DevicePlatform.IOS);

        assertThat(existing.getMember()).isSameAs(newOwner);
        verify(deviceTokenRepository, never()).save(any(DeviceToken.class));
    }
}
