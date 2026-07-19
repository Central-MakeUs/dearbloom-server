package kr.co.dearbloom.global.dev.dto;

import kr.co.dearbloom.domain.artist.dto.response.ArtistResponse;
import kr.co.dearbloom.domain.customer.dto.response.CustomerResponse;
import kr.co.dearbloom.domain.member.dto.MemberInfoResponse;

public record DevMemberFullInfoResponse(
        MemberInfoResponse member,
        CustomerResponse customer,
        ArtistResponse artist
) {
}
