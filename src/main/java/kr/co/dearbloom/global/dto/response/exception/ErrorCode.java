package kr.co.dearbloom.global.dto.response.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    /**
     * OauthAccount
     */
    NAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "OauthAccount-409", "이미 존재하는 이름입니다."),

    /**
     * Member
     */
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER-404", "Member를 찾을 수 없습니다."),
    REQUEST_UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "MEMBER-401", "요청 권한이 없는 사용자입니다."),
    NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "MEMBER-409", "이미 존재하는 닉네임입니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "MEMBER-409", "이미 존재하는 이메일입니다."),

    /**
     * Notification
     */
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTIFICATION-404", "알림을 찾을 수 없거나 접근 권한이 없습니다."),

    /**
     * 인증
     */
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-401", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-401", "액세스 토큰이 만료되었습니다."),
    INVALID_OAUTH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-401", "소셜 로그인 토큰이 유효하지 않습니다."),
    UNSUPPORTED_OAUTH_PROVIDER(HttpStatus.BAD_REQUEST, "AUTH-400", "지원하지 않는 OAuth 프로바이더입니다."),

    /**
     * 프론트엔드 오류
     */
    EMPTY_CHECKED_LIST(HttpStatus.BAD_REQUEST, "CHECKED-LIST-400", "빈 배열은 허용되지 않습니다."),
    PARAMETER_BAD_REQUEST(HttpStatus.BAD_REQUEST, "PARAMETER-400", "잘못된 파라미터 입력입니다."),
    INVALID_CURSOR(HttpStatus.BAD_REQUEST, "CURSOR-400", "잘못된 cursor 입니다."),
    INVALID_FILE_URL(HttpStatus.BAD_REQUEST, "FILE-400", "파일 URL은 허용된 CDN 경로로 시작해야 합니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
