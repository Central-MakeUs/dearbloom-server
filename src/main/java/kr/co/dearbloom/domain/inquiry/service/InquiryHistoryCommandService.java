package kr.co.dearbloom.domain.inquiry.service;

import kr.co.dearbloom.domain.inquiry.entity.inquiry.Inquiry;
import kr.co.dearbloom.domain.inquiry.entity.inquiry.InquiryHistory;
import kr.co.dearbloom.domain.inquiry.entity.inquiry.InquiryStatus;
import kr.co.dearbloom.domain.inquiry.repository.InquiryHistoryRepository;
import kr.co.dearbloom.domain.member.entity.MemberRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 문의 상태 변경 이력 append. 전이 직후 호출한다(toStatus 는 현재 inquiry 상태에서 읽음). */
@Service
@RequiredArgsConstructor
@Transactional
public class InquiryHistoryCommandService {
    private final InquiryHistoryRepository inquiryHistoryRepository;

    /**
     * 상태 변경 1건 기록. fromStatus 는 전이 전 상태(최초 생성이면 null), toStatus 는 inquiry 의 현재 상태.
     * changedByRole 은 변경 주체(고객/작가).
     */
    public void record(Inquiry inquiry, InquiryStatus fromStatus, MemberRole changedByRole) {
        inquiryHistoryRepository.save(InquiryHistory.builder()
                .inquiry(inquiry)
                .fromStatus(fromStatus)
                .toStatus(inquiry.getStatus())
                .changedByRole(changedByRole)
                .build());
    }
}
