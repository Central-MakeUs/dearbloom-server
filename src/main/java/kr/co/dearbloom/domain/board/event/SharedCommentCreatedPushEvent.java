package kr.co.dearbloom.domain.board.event;

/**
 * 공동보드에 댓글이 달림 → 작성자를 뺀 나머지 참여자 전원에게 푸시.
 *
 * <p><b>엔티티가 아니라 값만 담는다.</b> AFTER_COMMIT 리스너는 트랜잭션 밖이고 {@code open-in-view=false}
 * 라서, 엔티티를 넘기면 지연 로딩 시점에 {@code LazyInitializationException} 이 난다.
 *
 * <p>수신자 목록은 담지 않는다 — 댓글 등록 트랜잭션에 조회를 얹지 않으려고 리스너가 자기 트랜잭션에서 찾는다.
 * 대신 <b>누구를 빼야 하는지</b>({@code authorCustomerId})는 여기서 알려 준다.
 *
 * <p>{@code authorName} 은 발송 문구에 쓸 작성자 이름이다. 리스너에서 다시 조회하지 않도록
 * 이미 로딩된 값을 그대로 실어 보낸다.
 */
public record SharedCommentCreatedPushEvent(
        Long sharedBoardId,
        Long authorCustomerId,
        String authorName,
        String content
) {
}
