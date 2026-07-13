package kr.co.dearbloom.global.dev.dto;

import kr.co.dearbloom.domain.artist.dto.response.ArtistProfileResponse;
import kr.co.dearbloom.domain.customer.dto.response.CustomerInfoResponse;
import kr.co.dearbloom.domain.member.dto.MemberInfoResponse;

public record DevMemberFullInfoResponse(
        MemberInfoResponse member,
        CustomerInfoResponse customer,
        ArtistProfileResponse artist
) {
}
