package kr.co.dearbloom.domain.notification.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kr.co.dearbloom.domain.notification.entity.DevicePlatform;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DeviceTokenRegisterRequest {
    @NotBlank
    @Schema(description = "FCM registration token. 앱(네이티브 셸)이 Firebase 에서 받아 넘긴 값입니다.",
            example = "fL9x...:APA91b...")
    private String token;

    @NotNull
    @Schema(description = "기기 플랫폼. 현재는 IOS 만 실제로 발송됩니다.", example = "IOS")
    private DevicePlatform platform;
}
