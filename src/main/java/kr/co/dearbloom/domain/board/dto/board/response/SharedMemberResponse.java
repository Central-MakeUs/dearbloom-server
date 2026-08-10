package kr.co.dearbloom.domain.board.dto.board.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.dearbloom.domain.board.entity.board.SharedMember;
import kr.co.dearbloom.domain.customer.entity.CustomerProfileImage;

/** 공동보드 참여자 한 명. */
public record SharedMemberResponse(
        @Schema(description = "참여자 고객 ID", example = "3")
        Long customerId,

        @Schema(description = "공유 멤버 이름", example = "김디어")
        String sharedMemberName,

        @Schema(description = "공유 멤버 기본 프로필 이미지 색상", example = "GREEN")
        CustomerProfileImage profileColor
) {
    public static SharedMemberResponse from(SharedMember sharedMember) {
        return new SharedMemberResponse(
                sharedMember.getCustomer().getCustomerId(),
                sharedMember.getCustomer().getName(),
                sharedMember.getCustomer().getProfileColor());
    }
}
