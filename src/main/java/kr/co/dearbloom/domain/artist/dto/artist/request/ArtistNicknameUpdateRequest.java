package kr.co.dearbloom.domain.artist.dto.artist.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import kr.co.dearbloom.global.validation.validatator.ValidNickname;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ArtistNicknameUpdateRequest {
    @NotBlank
    @ValidNickname
    @Schema(description = "새 닉네임 (2-12자의 한글, 영문, 숫자, _, 단어 사이 공백). "
            + "앞뒤 공백과 연속 공백은 사용할 수 없습니다.",
            example = "블루밍데이즈 스냅")
    private String nickname;
}
