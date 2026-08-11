package kr.co.dearbloom.domain.chat.entity;

import kr.co.dearbloom.domain.member.entity.MemberRole;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 메시지별 읽음 판정({@link ChatRoom#isReadByReceiver}) 검증.
 * 1:1 방이라 메시지 컬럼 없이 "발신 시각 <= 수신자의 마지막 읽은 시각" 으로 계산한다.
 */
class ChatRoomReadTest {
    private static final LocalDateTime NINE = LocalDateTime.of(2026, 6, 11, 9, 0);
    private static final LocalDateTime NINE_THIRTY = LocalDateTime.of(2026, 6, 11, 9, 30);
    private static final LocalDateTime TEN = LocalDateTime.of(2026, 6, 11, 10, 0);

    private ChatRoom room(LocalDateTime customerLastReadAt, LocalDateTime artistLastReadAt) {
        return ChatRoom.builder()
                .customerLastReadAt(customerLastReadAt)
                .artistLastReadAt(artistLastReadAt)
                .build();
    }

    @Test
    void 고객_메시지는_작가의_마지막_읽은_시각으로_판정한다() {
        // 작가가 09:30 까지 읽음 → 09:00 메시지는 읽음, 10:00 메시지는 안읽음
        ChatRoom room = room(null, NINE_THIRTY);

        assertThat(room.isReadByReceiver(MemberRole.CUSTOMER, NINE)).isTrue();
        assertThat(room.isReadByReceiver(MemberRole.CUSTOMER, TEN)).isFalse();
    }

    @Test
    void 작가_메시지는_고객의_마지막_읽은_시각으로_판정한다() {
        // 고객이 09:30 까지 읽음. 작가 쪽 값(artistLastReadAt=TEN)에 영향받지 않아야 한다.
        ChatRoom room = room(NINE_THIRTY, TEN);

        assertThat(room.isReadByReceiver(MemberRole.ARTIST, NINE)).isTrue();
        assertThat(room.isReadByReceiver(MemberRole.ARTIST, TEN)).isFalse();
    }

    @Test
    void 수신자가_방을_한_번도_읽지_않았으면_안읽음이다() {
        ChatRoom room = room(null, null);

        assertThat(room.isReadByReceiver(MemberRole.CUSTOMER, NINE)).isFalse();
        assertThat(room.isReadByReceiver(MemberRole.ARTIST, NINE)).isFalse();
    }

    @Test
    void 읽은_시각과_발신_시각이_같으면_읽음이다() {
        ChatRoom room = room(NINE, NINE);

        assertThat(room.isReadByReceiver(MemberRole.CUSTOMER, NINE)).isTrue();
        assertThat(room.isReadByReceiver(MemberRole.ARTIST, NINE)).isTrue();
    }

    @Test
    void 발신_시각이_없으면_안읽음으로_본다() {
        ChatRoom room = room(TEN, TEN);

        assertThat(room.isReadByReceiver(MemberRole.CUSTOMER, null)).isFalse();
        assertThat(room.isReadByReceiver(MemberRole.ARTIST, null)).isFalse();
    }
}
