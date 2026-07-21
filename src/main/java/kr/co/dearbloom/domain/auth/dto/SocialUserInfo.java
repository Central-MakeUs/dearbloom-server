package kr.co.dearbloom.domain.auth.dto;

/** 소셜 로그인(구글/애플) 검증 결과로 얻은 사용자 정보. */
public record SocialUserInfo(String sub, String email, String name) {}
