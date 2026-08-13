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
                .artist(artist)
                .artwork(artwork)
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
                .finalPhotoCountSnapshot(artworkPackage.getFinalPhotoCount())
                .build());
    }

    /**
     * 작품 삭제 시 그 작품을 참조하는 문의들의 작품·패키지 FK 를 끊는다(작품 삭제 전 호출).
     * 문의 행은 스냅샷으로 표시가 유지되므로 지우지 않는다 — 상대방의 거래 이력이기 때문.
     * 작가 참조는 끊지 않아서 작가 문의함과 예약 슬롯 계산에는 계속 잡힌다.
     */
    public void detachArtwork(Artwork artwork) {
        inquiryRepository.findByArtwork(artwork.getArtworkId())
                .forEach(Inquiry::detachArtwork);
        // 참조 해제 UPDATE 를 먼저 DB 에 반영한다. 뒤따르는 삭제가 FK 제약에 걸리지 않도록 순서를 못 박는 것.
        inquiryRepository.flush();
    }

    /**
     * 패키지 교체 시 그 작품의 문의들이 들고 있던 패키지 참조를 끊는다(기존 패키지 행 삭제 전 호출).
     * 끊지 않으면 FK 제약에 걸려 패키지를 지울 수 없다. 작품 참조는 살려두므로 상세 이동 링크는 유지된다.
     */
    public void detachArtworkPackages(Artwork artwork) {
        inquiryRepository.findByArtwork(artwork.getArtworkId())
                .forEach(Inquiry::detachArtworkPackage);
        // 참조 해제 UPDATE 를 먼저 DB 에 반영한다. 뒤따르는 패키지 삭제가 FK 제약에 걸리지 않도록 순서를 못 박는 것.
        inquiryRepository.flush();
    }
}
