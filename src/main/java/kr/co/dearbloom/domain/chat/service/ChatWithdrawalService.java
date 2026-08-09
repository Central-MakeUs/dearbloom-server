package kr.co.dearbloom.domain.chat.service;

import kr.co.dearbloom.domain.artist.entity.artist.Artist;
import kr.co.dearbloom.domain.chat.repository.ChatMessageRepository;
import kr.co.dearbloom.domain.customer.entity.Customer;
import kr.co.dearbloom.global.file.FileCleaner;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원 탈퇴 시 채팅에 남은 이미지의 S3 객체를 지운다.
 * <p>
 * 대화 기록은 상대방의 거래 이력이라 <b>메시지 행은 그대로 남긴다.</b> 다만 사진에는 얼굴이 담겨 있고
 * CDN URL 만 알면 영구히 열람되므로 S3 객체는 지운다(이미지 메시지는 깨진 채로 남는다).
 * <p>
 * 지우는 대상은 <b>탈퇴자가 직접 올린 사진</b>뿐이다. 상대가 올린 사진은 그 사람의 데이터다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatWithdrawalService {
    private final ChatMessageRepository chatMessageRepository;
    private final FileCleaner fileCleaner;

    public void deleteUploadedImages(Customer customer) {
        fileCleaner.deleteAllQuietly(chatMessageRepository.findImageUrlsUploadedByCustomer(customer));
    }

    public void deleteUploadedImages(Artist artist) {
        fileCleaner.deleteAllQuietly(chatMessageRepository.findImageUrlsUploadedByArtist(artist));
    }
}
