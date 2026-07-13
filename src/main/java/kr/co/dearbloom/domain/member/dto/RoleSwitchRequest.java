package kr.co.dearbloom.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import kr.co.dearbloom.domain.member.entity.MemberRole;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RoleSwitchRequest {
    @NotNull
    @Schema(description = "전환할 역할 (해당 역할의 프로필이 이미 생성되어 있어야 함)", example = "ARTIST")
    private MemberRole role;
}
