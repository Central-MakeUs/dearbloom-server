package kr.co.dearbloom.domain.artwork.dto;

import kr.co.dearbloom.domain.artwork.entity.Artwork;

import java.util.List;

/**
 * 작품 목록 한 페이지. hasNext 판단과 초과분 잘라내기까지 끝난 상태다.
 * 페이지 크기와 "+1 개 더 가져와 다음 페이지 유무를 본다"는 요령이 조회 계층 밖으로 새지 않게 한다.
 */
public record ArtworkPage(
        List<Artwork> artworks,
        boolean hasNext
) {
}
