package kr.co.dearbloom.domain.inquiry.event;

import kr.co.dearbloom.domain.inquiry.entity.Inquiry;

/**
 * 스마트 문의가 생성되면 발행. 채팅 도메인이 구독해 방 find-or-create + 문의 카드 append 를 처리한다.
 * 동기 리스너로 같은 트랜잭션에서 실행 — 문의와 채팅 방/카드가 원자적으로 커밋된다.
 */
public final class InquiryCreatedEvent {
    private final Inquiry inquiry;
    private Long chatRoomId;

    public InquiryCreatedEvent(Inquiry inquiry) {
        this.inquiry = inquiry;
    }

    public Inquiry inquiry() {
        return inquiry;
    }

    /** 리스너가 만든(또는 찾은) 채팅방 ID. 리스너가 돌지 않았으면 null. */
    public Long chatRoomId() {
        return chatRoomId;
    }

    public void chatRoomId(Long chatRoomId) {
        this.chatRoomId = chatRoomId;
    }
}
