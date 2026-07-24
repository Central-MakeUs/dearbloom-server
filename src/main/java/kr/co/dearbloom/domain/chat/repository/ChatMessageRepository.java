package kr.co.dearbloom.domain.chat.repository;

import kr.co.dearbloom.domain.chat.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    /**
     * 방 메시지 히스토리(id 내림차순, 커서 페이지네이션). cursor 가 null 이면 최신부터.
     * INQUIRY 카드 렌더용으로 문의·패키지·작품을 left join fetch(문의 없는 TEXT 도 보존, 모두 to-one 이라 페이징 안전).
     */
    @Query("""
            select m from ChatMessage m
            left join fetch m.inquiry i
            left join fetch i.artworkPackage p
            left join fetch p.artwork
            where m.chatRoom.chatRoomId = :roomId
              and (:cursor is null or m.chatMessageId < :cursor)
            order by m.chatMessageId desc
            """)
    List<ChatMessage> findHistory(@Param("roomId") Long roomId,
                                  @Param("cursor") Long cursor,
                                  Pageable pageable);
}
