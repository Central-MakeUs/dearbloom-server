package kr.co.dearbloom.global.dev.service;

import kr.co.dearbloom.domain.member.repository.OAuthAccountRepository;
import kr.co.dearbloom.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DevAccountService {
    private final OAuthAccountRepository oAuthAccountRepository;
    private final MemberRepository memberRepository;
}
