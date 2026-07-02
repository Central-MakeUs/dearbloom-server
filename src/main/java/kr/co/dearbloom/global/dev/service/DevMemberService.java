package kr.co.dearbloom.global.dev.service;

import kr.co.dearbloom.domain.artist.repository.artist.ArtistRepository;
import kr.co.dearbloom.domain.customer.repository.CustomerRepository;
import kr.co.dearbloom.domain.member.repository.OAuthAccountRepository;
import kr.co.dearbloom.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DevMemberService {
    private final OAuthAccountRepository oAuthAccountRepository;
    private final MemberRepository memberRepository;
    private final CustomerRepository customerRepository;
    private final ArtistRepository artistRepository;
}
