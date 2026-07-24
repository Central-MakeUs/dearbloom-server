package kr.co.dearbloom.domain.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatMessageSendRequest {
    @NotBlank
    @Size(max = 2000)
    @Schema(description = "메시지 본문 (텍스트)", example = "네 감사합니다!")
    private String content;
}
