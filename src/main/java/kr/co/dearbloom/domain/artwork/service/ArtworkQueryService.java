package kr.co.dearbloom.domain.artwork.service;

import kr.co.dearbloom.domain.artist.entity.Artist;
import kr.co.dearbloom.domain.artwork.dto.response.ArtworkThumbnailResponse;
import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.domain.artwork.entity.PortfolioFile;
import kr.co.dearbloom.domain.artwork.repository.ArtworkRepository;
import kr.co.dearbloom.domain.artwork.repository.PortfolioFileRepository;
import kr.co.dearbloom.global.dto.response.exception.CustomException;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArtworkQueryService {
    private final ArtworkRepository artworkRepository;
    private final PortfolioFileRepository portfolioFileRepository;

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
        // sortOrder 오름차순 조회 → 작품별 첫 사진이 대표 이미지. merge 시 먼저 들어온 값 유지.
        Map<Long, String> representativeImage = portfolioFileRepository
                .findByArtworkInOrderBySortOrderAsc(others).stream()
                .collect(Collectors.toMap(
                        file -> file.getArtwork().getArtworkId(),
                        PortfolioFile::getFileUrl,
                        (first, second) -> first));
        return others.stream()
                .map(artwork -> new ArtworkThumbnailResponse(
                        artwork.getArtworkId(),
                        representativeImage.get(artwork.getArtworkId())))
                .toList();
    }
}
