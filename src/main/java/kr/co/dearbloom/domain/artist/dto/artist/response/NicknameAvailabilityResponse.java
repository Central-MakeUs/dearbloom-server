package kr.co.dearbloom.domain.artist.dto.artist.response;

import io.swagger.v3.oas.annotations.media.Schema;

/** 작가 닉네임 사용 가능 여부. */
public record NicknameAvailabilityResponse(
        @Schema(description = "이 닉네임으로 등록/수정할 수 있으면 true. 이미 다른 작가가 쓰고 있으면 false.",
                example = "true")
        boolean available
) {
}
