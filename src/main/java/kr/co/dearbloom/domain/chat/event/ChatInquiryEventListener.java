package kr.co.dearbloom.domain.chat.event;

import kr.co.dearbloom.domain.chat.facade.ChatFacade;
import kr.co.dearbloom.domain.inquiry.event.InquiryCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** 문의 생성 이벤트를 받아 채팅 방·카드를 만든다. 문의 트랜잭션 안에서 동기 실행(원자성). */
@Component
@RequiredArgsConstructor
public class ChatInquiryEventListener {
    private final ChatFacade chatFacade;

    @EventListener
    public void onInquiryCreated(InquiryCreatedEvent event) {
        chatFacade.onInquiryCreated(event.inquiry());
    }
}
