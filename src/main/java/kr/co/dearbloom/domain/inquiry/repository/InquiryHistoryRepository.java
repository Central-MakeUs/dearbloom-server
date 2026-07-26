package kr.co.dearbloom.domain.inquiry.repository;

import kr.co.dearbloom.domain.inquiry.entity.InquiryHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InquiryHistoryRepository extends JpaRepository<InquiryHistory, Long> {
    // 특정 문의의 상태 변경 타임라인(오래된 순).
    List<InquiryHistory> findByInquiry_InquiryIdOrderByCreatedAtAsc(Long inquiryId);
}
