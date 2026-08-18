package kr.co.dearbloom.domain.artwork.event;

/**
 * 작품 탐색 첫 화면에 보이는 내용이 바뀌었다 — 캐시를 버려야 한다는 신호.
 * <p>
 * <b>내용이 없다.</b> 캐시 키가 하나뿐이라(전체 작품 최신순 첫 페이지) 무엇이 어떻게 바뀌었는지 알 필요가 없다.
 * 무효화가 곧 키 하나 삭제라, 이벤트에 작품 ID 나 작가 ID 를 실어도 쓸 데가 없다.
 * <p>
 * 발행 지점은 카드에 노출되는 값을 바꾸는 곳 전부다 —
 * 작품 등록/작품명 수정/사진 교체/패키지 교체(가격)/작품 삭제, 작가 닉네임·활동지역 수정, 작가 탈퇴.
 *
 * @see kr.co.dearbloom.domain.artwork.event.ArtworkExploreCacheEvictListener
 */
public record ArtworkExploreChangedEvent() {
}
