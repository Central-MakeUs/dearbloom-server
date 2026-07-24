package kr.co.dearbloom.domain.chat.service;

import kr.co.dearbloom.domain.artist.entity.artist.Artist;
import kr.co.dearbloom.domain.chat.entity.ChatRoom;
import kr.co.dearbloom.domain.chat.repository.ChatRoomRepository;
import kr.co.dearbloom.domain.customer.entity.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatRoomCommandService {
    private final ChatRoomRepository chatRoomRepository;

    /**
     * (고객, 작가) 방을 찾거나 없으면 생성. 문의 생성 트랜잭션에서 호출된다.
     * unique(customer_id, artist_id) 가 안전망 — 동일 쌍 동시 첫 문의(희귀)는 제약 위반으로 롤백되고 재시도된다.
     */
    public ChatRoom findOrCreate(Customer customer, Artist artist) {
        return chatRoomRepository.findByPair(customer.getCustomerId(), artist.getArtistId())
                .orElseGet(() -> chatRoomRepository.save(ChatRoom.create(customer, artist)));
    }
}
