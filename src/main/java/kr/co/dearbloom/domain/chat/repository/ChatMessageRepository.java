package kr.co.dearbloom.domain.chat.repository;

import kr.co.dearbloom.domain.artist.entity.artist.Artist;
import kr.co.dearbloom.domain.chat.entity.ChatMessage;
import kr.co.dearbloom.domain.customer.entity.Customer;
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

    /**
     * 이 고객이 <b>직접 올린</b> 채팅 이미지 URL. 탈퇴 시 S3 객체를 지우기 위한 수집용.
     * 상대(작가)가 올린 사진은 그 사람의 데이터라 건드리지 않는다.
     */
    @Query("select m.imageUrl from ChatMessage m where m.chatRoom.customer = :customer"
            + " and m.senderRole = kr.co.dearbloom.domain.member.entity.MemberRole.CUSTOMER"
            + " and m.imageUrl is not null")
    List<String> findImageUrlsUploadedByCustomer(@Param("customer") Customer customer);

    /** 이 작가가 직접 올린 채팅 이미지 URL(탈퇴 시 S3 정리용). */
    @Query("select m.imageUrl from ChatMessage m where m.chatRoom.artist = :artist"
            + " and m.senderRole = kr.co.dearbloom.domain.member.entity.MemberRole.ARTIST"
            + " and m.imageUrl is not null")
    List<String> findImageUrlsUploadedByArtist(@Param("artist") Artist artist);
}
