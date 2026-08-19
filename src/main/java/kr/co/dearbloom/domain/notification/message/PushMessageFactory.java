package kr.co.dearbloom.domain.notification.message;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 알림 종류별 문구와 딥링크를 한 곳에 모은다.
 *
 * <p>본문에 실명·학교명 같은 개인정보를 넣지 않는다 — 잠금화면에 그대로 뜨기 때문이다.
 * 누가 보냈는지가 아니라 <b>언제 촬영인지</b>만 담는다.
 */
@Component
public class PushMessageFactory {
    private static final DateTimeFormatter SCHEDULE = DateTimeFormatter.ofPattern("M/d HH:mm");
    /** 댓글 미리보기 최대 길이. 잠금화면에서 잘리는 지점과 비슷하게 잡았다. */
    private static final int PREVIEW_MAX_LENGTH = 60;

    /** 문의가 새로 생성됨 → 해당 작가에게. */
    public PushMessage inquiryCreated(
            Long inquiryId, String artworkName, LocalDate shootDate, LocalTime startTime) {
        return PushMessage.of(
                "새 문의가 도착했어요",
                "[" + artworkName + "] " + schedule(shootDate, startTime) + " 촬영 문의예요.",
                "/app/artist/requests/" + inquiryId);
    }

    /** 문의가 예약 완료로 전환됨 → 해당 고객에게. */
    public PushMessage inquiryReserved(
            Long inquiryId, String artworkName, LocalDate shootDate, LocalTime startTime) {
        return PushMessage.of(
                "예약이 확정됐어요",
                "[" + artworkName + "] " + schedule(shootDate, startTime) + " 촬영이 예약 완료됐어요.",
                "/app/my/reservations/" + inquiryId);
    }

    /**
     * 공동보드에 댓글이 달림 → 작성자를 뺀 참여자 전원에게.
     * <p>
     * 앞머리는 <b>작성자 이름</b>이다. 잠금화면에 실명이 그대로 뜨지만, 보드는 초대받은 사람만 들어오는
     * 닫힌 공간이라 참여자끼리는 이미 서로 아는 사이다. 누가 말했는지가 빠지면 알림만 보고는 알 수 없다.
     */
    public PushMessage sharedCommentCreated(Long sharedBoardId, String authorName, String content) {
        return PushMessage.of(
                "새 댓글이 달렸어요",
                "[" + authorName + "] " + preview(content),
                "/app/boards/" + sharedBoardId);
    }

    /**
     * 긴 댓글을 잘라 낸다. 잠금화면이 어차피 잘라 보여 주는데,
     * 원문을 그대로 실으면 FCM 페이로드 상한(4KB)에 걸려 발송 자체가 실패할 수 있다.
     */
    private String preview(String content) {
        String single = content.replaceAll("\\s+", " ").trim();
        return single.length() <= PREVIEW_MAX_LENGTH
                ? single
                : single.substring(0, PREVIEW_MAX_LENGTH) + "…";
    }

    private String schedule(LocalDate shootDate, LocalTime startTime) {
        return shootDate.atTime(startTime).format(SCHEDULE);
    }
}
