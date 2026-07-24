package kr.co.dearbloom.domain.customer.service;

import kr.co.dearbloom.domain.customer.entity.Customer;
import kr.co.dearbloom.domain.customer.repository.CustomerRepository;
import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.domain.university.entity.University;
import kr.co.dearbloom.global.dto.response.exception.CustomException;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerCommandService {
    private final CustomerRepository customerRepository;

    // 온보딩. 실명·학교를 받아 고객 프로필을 만든다.
    // 과거 역할 해지로 익명화된 행이 남아 있으면(hasCustomer=false) 그 행을 되살린다(재가입=같은 사람 복귀).
    // markAsCustomer 이전에 호출되므로, 이미 활성 고객(hasCustomer=true)이 다시 부르면 중복으로 막는다.
    public Customer create(Member member, String name, University university) {
        Customer existing = customerRepository.findByMember(member).orElse(null);
        if (existing != null) {
            if (member.isHasCustomer()) {
                throw new CustomException(ErrorCode.CUSTOMER_ALREADY_EXISTS);
            }
            existing.reactivate(name, university);
            return existing;
        }
        return customerRepository.save(Customer.builder()
                .member(member)
                .name(name)
                .university(university)
                .build());
    }

    // 실명 수정. 중복 허용이라 유니크 검증 없음. managed 엔티티로 로드해 수정(응답 매핑 시 university LAZY 초기화 안전).
    public Customer updateName(Long customerId, String name) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomException(ErrorCode.CUSTOMER_NOT_FOUND));
        customer.updateName(name);
        return customer;
    }

    // 회원 탈퇴 시 이 멤버의 고객 프로필 익명화(있을 때만).
    public void anonymizeByMember(Member member) {
        customerRepository.findByMember(member).ifPresent(Customer::anonymize);
    }
}
