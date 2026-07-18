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
    public Customer create(Member member, String name, University university) {
        if (customerRepository.findByMember(member).isPresent()) { // 이미 고객 프로필이 존재하면 예외
            throw new CustomException(ErrorCode.CUSTOMER_ALREADY_EXISTS);
        }
        return customerRepository.save(Customer.builder()
                .member(member)
                .name(name)
                .university(university)
                .build());
    }
}
