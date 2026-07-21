package kr.co.dearbloom.domain.artist.dto.request;

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
    @Schema(description = "새 닉네임 (2-12자의 한글, 영문, 숫자, _)",
            example = "블룸작가")
    private String nickname;
}
