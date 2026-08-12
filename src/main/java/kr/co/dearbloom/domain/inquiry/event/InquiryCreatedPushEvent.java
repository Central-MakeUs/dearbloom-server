package kr.co.dearbloom.domain.inquiry.event;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 문의 생성 푸시용 이벤트. 수신자는 해당 <b>작가</b>.
 *
 * <p>{@link InquiryCreatedEvent} 와 별개로 두는 이유는 실행 시점이 다르기 때문이다. 그쪽은 채팅방 원자성 때문에
 * 같은 트랜잭션에서 동기로 돌고, 푸시는 커밋 이후 비동기로 돌아야 한다(롤백된 문의의 알림이 나가면 안 되고,
 * 트랜잭션 안에서 외부 HTTP 를 기다리면 DB 커넥션을 붙잡는다).
 *
 * <p><b>엔티티를 담지 않고 값만 담는다.</b> AFTER_COMMIT 리스너는 트랜잭션 밖이고 {@code open-in-view=false}
 * 라서, 엔티티를 넘기면 지연 로딩 시점에 {@code LazyInitializationException} 이 난다.
 */
public record InquiryCreatedPushEvent(
        Long inquiryId,
        Long artistMemberId,
        String artworkName,
        LocalDate shootDate,
        LocalTime startTime
) {
}
