package kr.co.dearbloom.domain.inquiry.repository;

import kr.co.dearbloom.domain.inquiry.entity.InquiryHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InquiryHistoryRepository extends JpaRepository<InquiryHistory, Long> {
}
