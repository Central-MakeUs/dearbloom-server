package kr.co.dearbloom.domain.artwork.service;

import kr.co.dearbloom.domain.artist.entity.artist.Artist;
import kr.co.dearbloom.domain.artist.entity.artist.Region;
import kr.co.dearbloom.domain.artist.util.BookingWindow;
import kr.co.dearbloom.domain.artwork.dto.ArtworkCursor;
import kr.co.dearbloom.domain.artwork.dto.ArtworkFilterCondition;
import kr.co.dearbloom.domain.artwork.dto.ArtworkPage;
import kr.co.dearbloom.domain.artwork.dto.request.ArtworkQueryRequest;
import kr.co.dearbloom.domain.artwork.dto.response.ArtistArtworkSummaryResponse;
import kr.co.dearbloom.domain.artwork.dto.response.ArtworkSummaryResponse;
import kr.co.dearbloom.domain.artwork.dto.response.ArtworkThumbnailResponse;
import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.domain.artwork.entity.ArtworkPackage;
import kr.co.dearbloom.domain.artwork.entity.PortfolioFile;
import kr.co.dearbloom.domain.artwork.repository.ArtworkPackageRepository;
import kr.co.dearbloom.domain.artwork.repository.ArtworkQueryRepository;
import kr.co.dearbloom.domain.artwork.repository.ArtworkRepository;
import kr.co.dearbloom.domain.artwork.repository.PortfolioFileRepository;
import kr.co.dearbloom.global.dto.response.exception.CustomException;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArtworkQueryService {
    // 촬영 희망 기간 최대 span. 화면 프리셋이 "오늘부터 30일"까지라 그 이상은 잘못된 요청으로 본다.
    private static final int MAX_DATE_RANGE_DAYS = 30;

    private final ArtworkRepository artworkRepository;
    private final ArtworkQueryRepository artworkQueryRepository;
    private final PortfolioFileRepository portfolioFileRepository;
    private final ArtworkPackageRepository artworkPackageRepository;

    // 소유권 검증 없이 작품을 조회한다(상세 조회용). 없으면 404.
    public Artwork getById(Long artworkId) {
        return artworkRepository.findById(artworkId)
                .orElseThrow(() -> new CustomException(ErrorCode.ARTWORK_NOT_FOUND));
    }

    // 작품을 조회하되 현재 작가 소유인지 검증한다. 없으면 404, 남의 작품이면 403.
    public Artwork getOwnedBy(Long artworkId, Artist artist) {
        Artwork artwork = artworkRepository.findById(artworkId)
                .orElseThrow(() -> new CustomException(ErrorCode.ARTWORK_NOT_FOUND));
        if (!artwork.getArtist().getArtistId().equals(artist.getArtistId())) {
            throw new CustomException(ErrorCode.ARTWORK_ACCESS_DENIED);
        }
        return artwork;
    }

    public List<PortfolioFile> getPortfolioFiles(Artwork artwork) {
        return portfolioFileRepository.findByArtworkOrderBySortOrderAsc(artwork);
    }

    public List<ArtworkPackage> getPackages(Artwork artwork) {
        return artworkPackageRepository.findByArtwork(artwork);
    }

    /**
     * 요청 파라미터를 쿼리 조건으로 해석한다. 날짜 범위 검증·요일 접기가 여기서 끝난다.
     * 페이지 조회와 개수 조회가 같은 조건 객체를 공유해야 총 개수가 목록과 어긋나지 않는다.
     */
    public ArtworkFilterCondition resolveCondition(ArtworkQueryRequest request) {
        return new ArtworkFilterCondition(
                resolveAvailableDayOfWeeks(request.getStartDate(), request.getEndDate()),
                request.getRegion(),
                request.getHeadCount(),
                request.getSort());
    }

    /**
     * 필터·정렬을 적용한 작품 페이지.
     * 한 페이지보다 한 개 더 가져와서, 그 초과분이 있으면 다음 페이지가 있다고 보고 잘라낸다.
     */
    public ArtworkPage findArtworkPage(ArtworkFilterCondition condition, ArtworkCursor cursor, int size) {
        List<Artwork> found = artworkQueryRepository.findArtworkPage(condition, cursor, size);
        boolean hasNext = found.size() > size;
        return new ArtworkPage(hasNext ? found.subList(0, size) : found, hasNext);
    }

    // 같은 필터를 만족하는 전체 작품 수.
    public long countArtworks(ArtworkFilterCondition condition) {
        return artworkQueryRepository.countArtworks(condition);
    }

    /**
     * 촬영 희망 기간을 "작가가 가능해야 하는 요일 집합"으로 접는다.
     * 기간이 연속이라 7일 이상이면 7요일 전부가 되고, 그래서 30일치 날짜를 쿼리에 늘어놓을 일이 없다.
     * 기간 중 <b>하루라도</b> 가능하면 노출하는 정책이라 요일들의 OR(=IN) 로 충분하다.
     *
     * <p>예약 오픈 창(오늘~3개월) 과 교집합을 먼저 잡는다 — 창 밖 날짜는 규칙과 무관하게 예약이 안 되기 때문이다.
     * 교집합이 비면 빈 Set 을 돌려주고(결과 0건), 날짜를 아예 안 보냈으면 null 을 돌려준다(필터 없음).
     */
    private Set<DayOfWeek> resolveAvailableDayOfWeeks(LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return null;
        }
        if (startDate == null || endDate == null || endDate.isBefore(startDate)
                || ChronoUnit.DAYS.between(startDate, endDate) > MAX_DATE_RANGE_DAYS) {
            throw new CustomException(ErrorCode.PARAMETER_BAD_REQUEST);
        }

        LocalDate from = maxDate(startDate, BookingWindow.firstOpenDate());
        LocalDate to = minDate(endDate, BookingWindow.lastOpenDate());
        if (to.isBefore(from)) {
            return Set.of();
        }

        Set<DayOfWeek> dayOfWeeks = EnumSet.noneOf(DayOfWeek.class);
        for (LocalDate date = from; !date.isAfter(to) && dayOfWeeks.size() < DayOfWeek.values().length;
             date = date.plusDays(1)) {
            dayOfWeeks.add(date.getDayOfWeek());
        }
        return dayOfWeeks;
    }

    private LocalDate maxDate(LocalDate left, LocalDate right) {
        return left.isAfter(right) ? left : right;
    }

    private LocalDate minDate(LocalDate left, LocalDate right) {
        return left.isBefore(right) ? left : right;
    }

    // 특정 작가의 작품을 최신순으로 작가용 카드(저장 수/조회수 포함)로 조회.
    public List<ArtistArtworkSummaryResponse> getArtistArtworkSummaries(Artist artist) {
        List<Artwork> artworks = artworkRepository.findByArtistWithArtistOrderByCreatedAtDesc(artist);
        if (artworks.isEmpty()) {
            return List.of();
        }
        Map<Long, String> representativeImage = representativeImageMap(artworks);
        return artworks.stream()
                .map(artwork -> new ArtistArtworkSummaryResponse(
                        artwork.getArtworkId(),
                        artwork.getArtworkName(),
                        artwork.getLowestPrice(),
                        artwork.getMinHeadCount(),
                        artwork.getMaxHeadCount(),
                        artwork.getArtist().getNickname(),
                        Region.toSortedNames(artwork.getArtist().getRegions()),
                        representativeImage.get(artwork.getArtworkId()),
                        artwork.getSavedCount(),
                        artwork.getViewCount()))
                .toList();
    }

    /**
     * 이 작가의 다른 작품(현재 작품 제외)을 저장 많은 순으로, 각 작품의 대표 이미지 1장과 함께 조회.
     * 대표 이미지는 sortOrder 가 가장 앞선 사진.
     */
    public List<ArtworkThumbnailResponse> getOtherArtworkThumbnails(Artist artist, Long excludeArtworkId) {
        List<Artwork> others =
                artworkRepository.findByArtistAndArtworkIdNotOrderBySavedCountDesc(artist, excludeArtworkId);
        if (others.isEmpty()) {
            return List.of();
        }
        Map<Long, String> representativeImage = representativeImageMap(others);
        // others 는 모두 같은 작가의 작품이라 닉네임은 파라미터 artist 로 공통 사용.
        String artistNickname = artist.getNickname();
        return others.stream()
                .map(artwork -> new ArtworkThumbnailResponse(
                        artwork.getArtworkId(),
                        artwork.getArtworkName(),
                        artistNickname,
                        representativeImage.get(artwork.getArtworkId())))
                .toList();
    }

    /**
     * 작품 목록을 리스트 카드로 변환. 넘겨받은 순서를 그대로 유지한다(정렬은 호출부 책임).
     * savedArtworkIds 가 null 이면 isSaved 는 전부 null(비로그인 등), 있으면 포함 여부로 채운다.
     */
    public List<ArtworkSummaryResponse> getSummaries(List<Artwork> artworks, Set<Long> savedArtworkIds) {
        if (artworks.isEmpty()) {
            return List.of();
        }
        Map<Long, String> representativeImage = representativeImageMap(artworks);
        return artworks.stream()
                .map(artwork -> new ArtworkSummaryResponse(
                        artwork.getArtworkId(),
                        artwork.getArtworkName(),
                        artwork.getLowestPrice(),
                        artwork.getMinHeadCount(),
                        artwork.getMaxHeadCount(),
                        artwork.getArtist().getNickname(),
                        Region.toSortedNames(artwork.getArtist().getRegions()),
                        representativeImage.get(artwork.getArtworkId()),
                        savedArtworkIds == null ? null : savedArtworkIds.contains(artwork.getArtworkId())))
                .toList();
    }

    // 작품별 대표 이미지(sortOrder 가장 앞선 사진) URL 맵. 배치 조회(다른 도메인에서도 재사용).
    public Map<Long, String> getRepresentativeImageUrls(List<Artwork> artworks) {
        return representativeImageMap(artworks);
    }

    // 작품 대표 이미지(sortOrder 가장 앞선 사진) URL 단건. 없으면 null.
    public String getRepresentativeImageUrl(Artwork artwork) {
        return portfolioFileRepository.findByArtworkOrderBySortOrderAsc(artwork).stream()
                .findFirst()
                .map(PortfolioFile::getFileUrl)
                .orElse(null);
    }

    // 작품별 대표 이미지(sortOrder 가장 앞선 사진) URL 맵. 한 번의 조회로 N+1 회피.
    private Map<Long, String> representativeImageMap(List<Artwork> artworks) {
        return portfolioFileRepository.findByArtworkInOrderBySortOrderAsc(artworks).stream()
                .collect(Collectors.toMap(
                        file -> file.getArtwork().getArtworkId(),
                        PortfolioFile::getFileUrl,
                        (first, second) -> first));
    }
}
