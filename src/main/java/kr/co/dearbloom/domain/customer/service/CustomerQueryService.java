package kr.co.dearbloom.domain.customer.service;

import kr.co.dearbloom.domain.customer.entity.Customer;
import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.domain.customer.repository.CustomerRepository;
import kr.co.dearbloom.global.dto.response.exception.CustomException;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerQueryService {
    private final CustomerRepository customerRepository;

    public Customer getById(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomException(ErrorCode.CUSTOMER_NOT_FOUND));
    }

    // 이 멤버의 고객 프로필(없을 수 있음). 탈퇴 정리처럼 있으면 처리하고 없으면 넘어가는 경로용.
    public Optional<Customer> findByMember(Member member) {
        return customerRepository.findByMember(member);
    }
}
