package kr.co.dearbloom.domain.chat.repository;

import kr.co.dearbloom.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    // (고객, 작가) 쌍으로 방 조회 — 문의 시 find-or-create 용.
    @Query("""
            select r from ChatRoom r
            where r.customer.customerId = :customerId
              and r.artist.artistId = :artistId
            """)
    Optional<ChatRoom> findByPair(@Param("customerId") Long customerId, @Param("artistId") Long artistId);

    // 고객 시점 방 목록(상대=작가). 닉네임/이미지 위해 작가 fetch join, 최근 메시지순.
    @Query("""
            select r from ChatRoom r
            join fetch r.artist a
            where r.customer.customerId = :customerId
            order by r.lastMessageAt desc
            """)
    List<ChatRoom> findCustomerRooms(@Param("customerId") Long customerId);

    // 작가 시점 방 목록(상대=고객). 이름 위해 고객 fetch join, 최근 메시지순.
    @Query("""
            select r from ChatRoom r
            join fetch r.customer c
            where r.artist.artistId = :artistId
            order by r.lastMessageAt desc
            """)
    List<ChatRoom> findArtistRooms(@Param("artistId") Long artistId);

    // WebSocket 구독 권한 검증용 — 트랜잭션 밖(인터셉터)에서 LAZY 없이 참여자 PK만 조회.
    @Query("""
            select r.customer.customerId as customerId, r.artist.artistId as artistId
            from ChatRoom r
            where r.chatRoomId = :roomId
            """)
    Optional<Participants> findParticipants(@Param("roomId") Long roomId);

    interface Participants {
        Long getCustomerId();
        Long getArtistId();
    }
}
