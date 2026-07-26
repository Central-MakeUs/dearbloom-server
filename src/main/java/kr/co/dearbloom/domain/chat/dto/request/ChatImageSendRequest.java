package kr.co.dearbloom.domain.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatImageSendRequest {
    @NotBlank
    @Schema(description = "업로드 완료된 이미지 CDN URL (File presigned 로 S3 업로드 후 받은 URL). 한 번에 한 장.",
            example = "https://cdn.dearbloom.co.kr/chat/image/abc.webp")
    private String imageUrl;
}
