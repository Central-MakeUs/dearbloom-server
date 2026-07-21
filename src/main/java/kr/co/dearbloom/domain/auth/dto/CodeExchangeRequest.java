package kr.co.dearbloom.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

/**
 * 하이브리드 로그인에서 발급된 1회용 oneTimeCode 를 토큰으로 교환하는 요청.
 * 로컬 Next.js 서버가 callback 에서 받은 oneTimeCode 를 담아 보낸다.
 * (Google 자체 authorization code 와 구분하기 위해 "code"가 아닌 "oneTimeCode"로 명명)
 */
@Getter
public class CodeExchangeRequest {
    @NotBlank
    private String oneTimeCode;
}
