package kr.co.dearbloom.domain.inquiry.event;

import kr.co.dearbloom.domain.inquiry.entity.Inquiry;

/**
 * 스마트 문의가 생성되면 발행. 채팅 도메인이 구독해 방 find-or-create + 문의 카드 append 를 처리한다.
 * 동기 리스너로 같은 트랜잭션에서 실행 — 문의와 채팅 방/카드가 원자적으로 커밋된다.
 */
public record InquiryCreatedEvent(Inquiry inquiry) {
}
