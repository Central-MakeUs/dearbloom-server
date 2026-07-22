package kr.co.dearbloom.domain.artwork.service;

import kr.co.dearbloom.domain.artist.entity.artist.Artist;
import kr.co.dearbloom.domain.artwork.dto.response.ArtistArtworkSummaryResponse;
import kr.co.dearbloom.domain.artwork.dto.response.ArtworkSummaryResponse;
import kr.co.dearbloom.domain.artwork.dto.response.ArtworkThumbnailResponse;
import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.domain.artwork.entity.ArtworkPackage;
import kr.co.dearbloom.domain.artwork.entity.PortfolioFile;
import kr.co.dearbloom.domain.artwork.repository.ArtworkPackageRepository;
import kr.co.dearbloom.domain.artwork.repository.ArtworkRepository;
import kr.co.dearbloom.domain.artwork.repository.PortfolioFileRepository;
import kr.co.dearbloom.global.dto.response.exception.CustomException;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArtworkQueryService {
    private final ArtworkRepository artworkRepository;
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

    // 전체 작품을 최신순으로 리스트 카드 형태로 조회. savedArtworkIds 는 고객이 저장한 작품 id 집합(없으면 null).
    public List<ArtworkSummaryResponse> getAllLatestSummaries(Set<Long> savedArtworkIds) {
        return getSummaries(artworkRepository.findAllWithArtistOrderByCreatedAtDesc(), savedArtworkIds);
    }

    // 특정 작가의 작품을 최신순으로 작가용 카드(저장 수/조회수 포함)로 조회.
    public List<ArtistArtworkSummaryResponse> getArtistArtworkSummaries(Artist artist) {
        List<Artwork> artworks = artworkRepository.findByArtistWithArtistOrderByCreatedAtDesc(artist);
        if (artworks.isEmpty()) {
            return List.of();
        }
        Map<Long, String> representativeImage = representativeImageMap(artworks);
        Map<Long, Integer> lowestPrice = lowestPriceMap(artworks);
        return artworks.stream()
                .map(artwork -> new ArtistArtworkSummaryResponse(
                        artwork.getArtworkId(),
                        artwork.getArtworkName(),
                        lowestPrice.get(artwork.getArtworkId()),
                        artwork.getMinHeadCount(),
                        artwork.getMaxHeadCount(),
                        artwork.getArtist().getNickname(),
                        artwork.getArtist().getRegions().stream().map(Enum::name).toList(),
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
        Map<Long, Integer> lowestPrice = lowestPriceMap(artworks);
        return artworks.stream()
                .map(artwork -> new ArtworkSummaryResponse(
                        artwork.getArtworkId(),
                        artwork.getArtworkName(),
                        lowestPrice.get(artwork.getArtworkId()),
                        artwork.getMinHeadCount(),
                        artwork.getMaxHeadCount(),
                        artwork.getArtist().getNickname(),
                        artwork.getArtist().getRegions().stream().map(Enum::name).toList(),
                        representativeImage.get(artwork.getArtworkId()),
                        savedArtworkIds == null ? null : savedArtworkIds.contains(artwork.getArtworkId())))
                .toList();
    }

    // 작품별 대표 이미지(sortOrder 가장 앞선 사진) URL 맵. 한 번의 조회로 N+1 회피.
    private Map<Long, String> representativeImageMap(List<Artwork> artworks) {
        return portfolioFileRepository.findByArtworkInOrderBySortOrderAsc(artworks).stream()
                .collect(Collectors.toMap(
                        file -> file.getArtwork().getArtworkId(),
                        PortfolioFile::getFileUrl,
                        (first, second) -> first));
    }

    // 작품별 최저 패키지 가격 맵. 한 번의 조회로 N+1 회피. 가격 null 패키지는 제외.
    private Map<Long, Integer> lowestPriceMap(List<Artwork> artworks) {
        return artworkPackageRepository.findByArtworkIn(artworks).stream()
                .filter(pkg -> pkg.getPrice() != null)
                .collect(Collectors.toMap(
                        pkg -> pkg.getArtwork().getArtworkId(),
                        ArtworkPackage::getPrice,
                        Math::min));
    }
}
