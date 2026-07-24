package kr.co.dearbloom.domain.inquiry.service;

import kr.co.dearbloom.domain.inquiry.entity.Inquiry;
import kr.co.dearbloom.domain.inquiry.entity.InquiryHistory;
import kr.co.dearbloom.domain.inquiry.entity.InquiryStatus;
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
     * changedByRole 은 변경 주체(고객/작가). 일반 사용자 조작이면 사유 없음(reason=null).
     */
    public void record(Inquiry inquiry, InquiryStatus fromStatus, MemberRole changedByRole) {
        record(inquiry, fromStatus, changedByRole, null);
    }

    /** 사유를 함께 남기는 기록. 탈퇴·해지로 인한 시스템 자동 전이에서 사용한다. */
    public void record(Inquiry inquiry, InquiryStatus fromStatus, MemberRole changedByRole, String reason) {
        inquiryHistoryRepository.save(InquiryHistory.builder()
                .inquiry(inquiry)
                .fromStatus(fromStatus)
                .toStatus(inquiry.getStatus())
                .changedByRole(changedByRole)
                .reason(reason)
                .build());
    }
}
