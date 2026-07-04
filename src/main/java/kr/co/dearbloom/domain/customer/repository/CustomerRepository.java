package kr.co.dearbloom.domain.customer.repository;

import kr.co.dearbloom.domain.customer.entity.Customer;
import kr.co.dearbloom.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByMember(Member member);
}
