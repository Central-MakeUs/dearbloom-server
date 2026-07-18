package kr.co.dearbloom.domain.customer.facade;

import kr.co.dearbloom.domain.auth.service.TokenService;
import kr.co.dearbloom.domain.customer.dto.request.CustomerCreateRequest;
import kr.co.dearbloom.domain.customer.dto.response.CustomerCreateResponse;
import kr.co.dearbloom.domain.customer.dto.response.CustomerInfoResponse;
import kr.co.dearbloom.domain.customer.entity.Customer;
import kr.co.dearbloom.domain.customer.service.CustomerCommandService;
import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.domain.member.entity.MemberRole;
import kr.co.dearbloom.domain.member.service.MemberCommandService;
import kr.co.dearbloom.domain.university.entity.University;
import kr.co.dearbloom.domain.university.service.UniversityQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CustomerFacade {
    private final CustomerCommandService customerCommandService;
    private final MemberCommandService memberCommandService;
    private final UniversityQueryService universityQueryService;
    private final TokenService tokenService;

    /**
     * 회원가입 직후의 토큰에는 고객 프로필이 없어 이후 @CurrentCustomer API 를 못 쓰므로
     * activeRole 이 CUSTOMER 로 갱신된 새 accessToken 을 함께 반환한다.
     */
    @Transactional
    public CustomerCreateResponse create(Member member, CustomerCreateRequest request) {
        University university = universityQueryService.findById(request.getUniversityId());
        Member updated = memberCommandService.markAsCustomer(member);
        Customer customer = customerCommandService.create(updated, request.getName(), university);
        return new CustomerCreateResponse(
                tokenService.createAccessToken(updated, MemberRole.CUSTOMER),
                CustomerInfoResponse.from(customer)
        );
    }
}
