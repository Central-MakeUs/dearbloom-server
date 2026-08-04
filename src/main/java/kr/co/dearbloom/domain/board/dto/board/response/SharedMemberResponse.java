package kr.co.dearbloom.domain.board.dto.board.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.dearbloom.domain.board.entity.board.SharedMember;

/** 공동보드 참여자 한 명(공유멤버 ID + 고객 이름). */
public record SharedMemberResponse(
        @Schema(description = "공유멤버 ID", example = "12")
        Long sharedMemberId,

        @Schema(description = "공유 멤버 이름", example = "김디어")
        String sharedMemberName
) {
    public static SharedMemberResponse from(SharedMember sharedMember) {
        return new SharedMemberResponse(
                sharedMember.getSharedMemberId(),
                sharedMember.getCustomer().getName());
    }
}
