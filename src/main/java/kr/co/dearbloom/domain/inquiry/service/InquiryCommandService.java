package kr.co.dearbloom.domain.inquiry.service;

import kr.co.dearbloom.domain.artist.entity.artist.Artist;
import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.domain.artwork.entity.ArtworkPackage;
import kr.co.dearbloom.domain.customer.entity.Customer;
import kr.co.dearbloom.domain.inquiry.entity.Inquiry;
import kr.co.dearbloom.domain.inquiry.repository.InquiryRepository;
import kr.co.dearbloom.domain.university.entity.University;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

/** 문의 생성/저장. 검증·엔티티 로딩은 파사드가 끝낸 뒤 넘겨준다. */
@Service
@RequiredArgsConstructor
@Transactional
public class InquiryCommandService {
    private final InquiryRepository inquiryRepository;

    /**
     * 문의 저장. 표시값(작가/작품/패키지/가격/소요시간)은 문의 시점 스냅샷으로 복사해 보존한다.
     * university 는 목록 선택 시에만, schoolName 은 항상 채워진다.
     */
    public Inquiry create(Customer customer, ArtworkPackage artworkPackage, University university, String schoolName,
                          LocalDate shootDate, LocalTime startTime, Integer headCount, String requestNote) {
        Artwork artwork = artworkPackage.getArtwork();
        Artist artist = artwork.getArtist();
        return inquiryRepository.save(Inquiry.builder()
                .customer(customer)
                .artworkPackage(artworkPackage)
                .university(university)
                .schoolName(schoolName)
                .shootDate(shootDate)
                .startTime(startTime)
                .durationMinutesSnapshot(artworkPackage.getDurationMinutes())
                .headCount(headCount)
                .requestNote(requestNote)
                .artistNicknameSnapshot(artist.getNickname())
                .artworkNameSnapshot(artwork.getArtworkName())
                .packageNameSnapshot(artworkPackage.getPackageName())
                .priceSnapshot(artworkPackage.getPrice())
                .build());
    }
}
