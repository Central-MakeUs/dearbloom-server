package kr.co.dearbloom.domain.board.dto.board.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** 공동보드 참여자 목록(멤버 리스트 + 인원). */
public record SharedMemberListResponse(
        @Schema(description = "공유 멤버 목록(입장 순). 방장도 포함됩니다.")
        List<SharedMemberResponse> sharedMemberList,

        @Schema(description = "참여 인원", example = "3")
        int sharedMemberCount
) {
    public static SharedMemberListResponse from(List<SharedMemberResponse> memberList) {
        return new SharedMemberListResponse(memberList, memberList.size());
    }
}
