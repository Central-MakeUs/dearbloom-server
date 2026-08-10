package kr.co.dearbloom.global.util;

import kr.co.dearbloom.global.dto.response.exception.CustomException;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 커서 페이지네이션용 인코더/디코더. 커서 객체 ↔ Base64(JSON) 문자열.
 * 커서에 담을 정렬 키는 목록마다 다르므로 타입을 받아 처리한다(도메인별 커서 record).
 * 클라이언트는 내용을 해석하지 말고 nextCursor 를 그대로 되돌려 보내면 된다.
 */
@Component
@RequiredArgsConstructor
public class CursorCodec {
    private final ObjectMapper objectMapper;

    public String encode(Object cursor) {
        try {
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(objectMapper.writeValueAsBytes(cursor));
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVALID_CURSOR);
        }
    }

    // 커서가 비어 있으면(첫 페이지) null 을 돌려준다. 깨진 커서는 400.
    public <T> T decode(String cursor, Class<T> type) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            byte[] json = Base64.getUrlDecoder().decode(cursor.getBytes(StandardCharsets.UTF_8));
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVALID_CURSOR);
        }
    }
}
