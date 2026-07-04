package kr.co.dearbloom.global.dev.dto;

import java.util.List;

public record SamplePageResponse(
        List<SampleMemberResponse> memberList,
        Long totalCount,
        Long totalPages,
        Long currentPage
) {
}
